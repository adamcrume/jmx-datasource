package com.adamcrume.jmxmon.telemetry;

import gov.nasa.arc.mct.api.feed.BufferFullException;
import gov.nasa.arc.mct.api.feed.FeedDataArchive;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.osgi.framework.BundleContext;

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


    private class Poller implements Runnable {
        @Override
        public void run() {
            try {
                int count = 0;
                int used = 0;
                while(true) {
                    Set<Feed> feeds;
                    Set<Feed> feedsToStop;
                    synchronized(activeComponents) {
                        feeds = new HashSet<Feed>(activeComponents.values());
                        feedsToStop = new HashSet<Feed>(recentlyStoppedComponents.values());
                        recentlyStoppedComponents.clear();
                    }
                    for(Feed feed : feeds) {
                        boolean reachable = false;
                        Object value = null;
                        try {
                            MBeanServerConnection connection = feed.getConnection();
                            String attribute = feed.getAttribute();
                            String[] parts = attribute.split("\\.");
                            value = connection.getAttribute(feed.getMbean(), parts[0]);
                            for(int i = 1; i < parts.length; i++) {
                                value = ((CompositeDataSupport) value).get(parts[i]);
                            }
                            reachable = true;
                        } catch(MalformedURLException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        } catch(AttributeNotFoundException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        } catch(InstanceNotFoundException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        } catch(MBeanException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        } catch(ReflectionException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        } catch(IOException e) {
                            LOGGER.log(Level.INFO, "Error reading MBean: " + e, e);
                        }
                        long time = System.currentTimeMillis();
                        Object oldValue = feed.getOldValue();
                        if(!equals(value, oldValue) || reachable != feed.isOldReachable()) {
                            if(!reachable) {
                                // This just reduces the occurrence of invalid string encodings.
                                value = oldValue;
                            }
                            addDatum(feed.getId(), time, value, reachable);
                            used++;
                        }
                        feed.setOldValue(oldValue);
                        feed.setOldReachable(reachable);
                        count++;
                    }
                    for(Feed feed : feedsToStop) {
                        addDatum(feed.getId(), System.currentTimeMillis() + 1, 0, false);
                    }
                    LOGGER.finest("Using " + (100.0 * used / count) + "% of values");
                    Thread.sleep(1000);
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
            }
        }
    }


    public void stop(TelemetryComponent component) {
        synchronized(activeComponents) {
            Feed feed = activeComponents.remove(component);
            if(feed != null) {
                recentlyStoppedComponents.put(component, feed);
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
    }
}
