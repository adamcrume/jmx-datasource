/*******************************************************************************
 * Copyright 2012 Adam Crume
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adamcrume.jmxmon.telemetry;

import gov.nasa.arc.mct.api.feed.BufferFullException;
import gov.nasa.arc.mct.api.feed.FeedDataArchive;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.osgi.framework.BundleContext;

import com.adamcrume.jmxmon.el.CompositeDataResolver;
import com.adamcrume.jmxmon.el.MBeanAttributeVariableMapper;
import com.adamcrume.jmxmon.el.SimpleContext;
import com.adamcrume.jmxmon.el.SimpleFunctionMapper;

public class DataPoller {
    private static final Logger LOGGER = Logger.getLogger(DataPoller.class.getName());

    private static final Color GOOD_COLOR = new Color(0, 138, 0);

    private static final Color LOS_COLOR = new Color(0, 72, 217);

    @Deprecated
    private static DataPoller instance;

    private Thread thread;

    private FeedDataArchive archive;

    private Map<TelemetryComponent, Feed> activeComponents = new HashMap<TelemetryComponent, Feed>();

    private Map<TelemetryComponent, Feed> recentlyStoppedComponents = new HashMap<TelemetryComponent, Feed>();

    private Map<String, MBeanServerConnection> connections = new HashMap<String, MBeanServerConnection>();

    private ExpressionFactory expressionFactory;


    @Deprecated
    public static DataPoller getInstance() {
        return instance;
    }


    // called by declarative services
    @SuppressWarnings("unused")
    private void activate(BundleContext context) {
        LOGGER.info("Activating " + getClass());
        instance = this;
        thread = new Thread(new Poller());
        thread.start();
    }


    // called by declarative services
    @SuppressWarnings("unused")
    private void deactivate() {
        LOGGER.info("Deactivating " + getClass());
        if(thread != null) {
            thread.interrupt();
        }
    }


    // called by declarative services
    @SuppressWarnings("unused")
    private void setDataArchive(FeedDataArchive archive) {
        this.archive = archive;
    }


    // called by declarative services
    @SuppressWarnings("unused")
    private void releaseDataArchive(FeedDataArchive archive) {
        this.archive = null;
    }


    // called by declarative services
    @SuppressWarnings("unused")
    private void setExpressionFactory(ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
    }


    // called by declarative services
    @SuppressWarnings("unused")
    private void releaseExpressionFactory(ExpressionFactory expressionFactory) {
        this.expressionFactory = null;
    }


    private class Poller implements Runnable {
        @Override
        public void run() {
            try {
                int count = 0;
                int used = 0;
                FunctionMapper functionMapper = new SimpleFunctionMapper();
                CompositeELResolver resolver = new CompositeELResolver();
                resolver.add(new CompositeDataResolver());
                resolver.add(new BeanELResolver());
                while(true) {
                    Set<Feed> feeds;
                    Set<Feed> feedsToStop;
                    synchronized(activeComponents) {
                        feeds = new HashSet<Feed>(activeComponents.values());
                        feedsToStop = new HashSet<Feed>(recentlyStoppedComponents.values());
                        recentlyStoppedComponents.clear();
                    }
                    for(Feed feed : feeds) {
                        long now = System.currentTimeMillis();
                        if(feed.getNextPollTime() > now) {
                            continue;
                        }
                        boolean reachable = false;
                        Object value = null;
                        try {
                            MBeanServerConnection connection = feed.getConnection();
                            String attribute = feed.getAttribute();
                            LOGGER.finest("Polling " + attribute);
                            ObjectName mbean = feed.getMbean();
                            VariableMapper variableMapper = new MBeanAttributeVariableMapper(connection, mbean);
                            ELContext context = new SimpleContext(functionMapper, resolver, variableMapper);
                            ValueExpression ve = expressionFactory.createValueExpression(context,"${"+ attribute+"}", Object.class);
                            value = ve.getValue(context);
                            reachable = true;
                        } catch(ELException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        }
                        now = System.currentTimeMillis();
                        Object oldValue = feed.getOldValue();
                        if(!equals(value, oldValue) || reachable != feed.isOldReachable()) {
                            if(!reachable) {
                                // This just reduces the occurrence of invalid string encodings.
                                value = oldValue;
                            }
                            addDatum(feed.getId(), now, value, reachable);
                            used++;
                        }
                        feed.setOldValue(oldValue);
                        feed.setOldReachable(reachable);
                        feed.setNextPollTime(now + feed.getPollingInterval());
                        count++;
                    }
                    long nextPollTime = Long.MAX_VALUE;
                    for(Feed feed : feeds) {
                        nextPollTime = Math.min(nextPollTime, feed.getNextPollTime());
                    }
                    long now = System.currentTimeMillis();
                    for(Feed feed : feedsToStop) {
                        addDatum(feed.getId(), now + 1, 0, false);
                    }
                    LOGGER.finest("Using " + (100.0 * used / count) + "% of values");
                    long wait = nextPollTime - now;
                    if(wait > 0) {
                        synchronized(activeComponents) {
                            activeComponents.wait(wait);
                        }
                    }
                }
            } catch(InterruptedException e) {
                LOGGER.info(getClass() + " Interrupted");
            } finally {
                for(Feed feed : activeComponents.values()) {
                    addDatum(feed.getId(), System.currentTimeMillis() + 1, 0, false);
                }
                thread = null;
            }
        }


        private boolean equals(Object a, Object b) {
            return a == null ? b == null : a.equals(b);
        }


        // TODO: Figure out how to stuff data into the other buffers
        private void addDatum(String feedID, long time, Object value, boolean valid) {
            Map<String, String> datum = new HashMap<String, String>();
            datum.put(FeedProvider.NORMALIZED_IS_VALID_KEY, Boolean.toString(valid));
            String status = valid ? " " : "S";
            Color c = valid ? GOOD_COLOR : LOS_COLOR;
            String valueString = value == null ? "" : value.toString();
            RenderingInfo ri = new RenderingInfo(valueString, c, status, c, valid);
            ri.setPlottable(valid);
            datum.put(FeedProvider.NORMALIZED_RENDERING_INFO, ri.toString());

            datum.put(FeedProvider.NORMALIZED_TIME_KEY, Long.toString(time));
            datum.put(FeedProvider.NORMALIZED_VALUE_KEY, valueString);
            datum.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

            // TODO: Buffer data (for a short period) if the archive isn't available
            if(archive != null) {
                try {
                    archive.putData(TelemetryComponent.TelemetryPrefix + feedID, TimeUnit.MILLISECONDS, time, datum);
                } catch(BufferFullException e) {
                    // TODO: What do we do?
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                }
            }
        }
    }


    public void start(TelemetryComponent component) throws IOException, MalformedObjectNameException {
        synchronized(activeComponents) {
            if(!activeComponents.containsKey(component)) {
                activeComponents.put(component, new Feed(component));
                activeComponents.notifyAll();
            }
        }
    }


    public void stop(TelemetryComponent component) {
        synchronized(activeComponents) {
            Feed feed = activeComponents.remove(component);
            if(feed != null) {
                recentlyStoppedComponents.put(component, feed);
                activeComponents.notifyAll();
            }
        }
    }


    private class Feed {
        private String attribute;

        private ObjectName mbean;

        private MBeanServerConnection connection;

        private Object oldValue;

        private boolean oldReachable;

        private String id;

        private long nextPollTime = System.currentTimeMillis();

        private long pollingInterval;


        public Feed(TelemetryComponent component) throws IOException, MalformedObjectNameException {
            List<AbstractComponent> subs = component.getComponents();
            BeanDescriptorComponent bd = null;
            JVMComponent jvm = null;
            for(AbstractComponent c : subs) {
                if(c instanceof BeanDescriptorComponent) {
                    bd = (BeanDescriptorComponent) c;
                } else if(c instanceof JVMComponent) {
                    jvm = (JVMComponent) c;
                }
            }

            String jmxURL = jvm.getModel().getJmxURL();
            synchronized(connections) {
                MBeanServerConnection connection = connections.get(jmxURL);
                if(connection == null) {
                    JMXServiceURL url = new JMXServiceURL(jmxURL);
                    JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
                    connection = jmxc.getMBeanServerConnection();
                    connections.put(jmxURL, connection);
                }
                this.connection = connection;
            }
            this.attribute = bd.getModel().getAttribute();
            this.mbean = new ObjectName(bd.getModel().getBean());
            this.id = component.getId();
            this.pollingInterval = component.getModel().getPollingInterval();
        }


        public String getId() {
            return id;
        }


        public String getAttribute() {
            return attribute;
        }


        public ObjectName getMbean() {
            return mbean;
        }


        public MBeanServerConnection getConnection() {
            return connection;
        }


        public Object getOldValue() {
            return oldValue;
        }


        public void setOldValue(Object oldValue) {
            this.oldValue = oldValue;
        }


        public boolean isOldReachable() {
            return oldReachable;
        }


        public void setOldReachable(boolean oldReachable) {
            this.oldReachable = oldReachable;
        }


        public long getNextPollTime() {
            return nextPollTime;
        }


        public void setNextPollTime(long nextPollTime) {
            this.nextPollTime = nextPollTime;
        }

        public long getPollingInterval() {
            return pollingInterval;
        }
    }
}
