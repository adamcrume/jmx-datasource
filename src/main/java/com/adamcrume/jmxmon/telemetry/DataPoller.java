package com.adamcrume.jmxmon.telemetry;

import gov.nasa.arc.mct.api.feed.BufferFullException;
import gov.nasa.arc.mct.api.feed.FeedDataArchive;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
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
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.osgi.framework.BundleContext;

public class DataPoller {
    private static final Logger LOGGER = Logger.getLogger(DataPoller.class.getName());

    private static final Color GOOD_COLOR = new Color(0, 138, 0);

    private static final Color LOS_COLOR = new Color(0, 72, 217);

    private Thread thread;

    private FeedDataArchive archive;


    // called by declarative services
    @SuppressWarnings("unused")
    private void activate(BundleContext context) {
        LOGGER.info("Activating " + getClass());
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
                // TODO: Support customizable JMX URLs
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9998/jmxrmi");
                JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

                MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

                // TODO: Support customizable MBeans and attributes
                MemoryMXBean membean = ManagementFactory.newPlatformMXBeanProxy(mbsc, "java.lang:type=Memory",
                        MemoryMXBean.class);
                ObjectName osbeanName = new ObjectName("java.lang:type=OperatingSystem");
                ThreadMXBean threadbean = ManagementFactory.newPlatformMXBeanProxy(mbsc, "java.lang:type=Threading",
                        ThreadMXBean.class);
                OperatingSystemMXBean osbean = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                        "java.lang:type=OperatingSystem", OperatingSystemMXBean.class);
                RuntimeMXBean runtimebean = ManagementFactory.newPlatformMXBeanProxy(mbsc, "java.lang:type=Runtime",
                        RuntimeMXBean.class);
                int nCPUs = osbean.getAvailableProcessors();
                long prevUptime = runtimebean.getUptime();
                long prevCpuTime = (long) (Long) mbsc.getAttribute(osbeanName, "ProcessCpuTime");
                double prevValue = Double.NaN;
                int count = 0;
                int used = 0;
                while(true) {
                    long uptime = runtimebean.getUptime();
                    long elapsedTime = uptime - prevUptime;
                    long cpuTime = (long) (Long) mbsc.getAttribute(osbeanName, "ProcessCpuTime");
                    long elapsedCPUTime = cpuTime - prevCpuTime;
                    double usage = elapsedCPUTime / 1000000.0 / (elapsedTime * nCPUs);


                    // TODO: Flush old data
                    long time = System.currentTimeMillis();
                    double value = membean.getHeapMemoryUsage().getUsed();
                    if(value != prevValue) {
                        addDatum(time, value, true);
                        used++;
                    }
                    count++;
                    LOGGER.finest("Using " + (100.0 * used / count) + "% of values");


                    prevUptime = uptime;
                    prevCpuTime = cpuTime;
                    prevValue = value;
                    Thread.sleep(1000);
                }
            } catch(InterruptedException e) {
                LOGGER.info(getClass() + " Interrupted");
            } catch(MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(AttributeNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(InstanceNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(MBeanException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(ReflectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(MalformedObjectNameException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                addDatum(System.currentTimeMillis() + 1, 0, false);
                thread = null;
            }
        }


        // TODO: Figure out how to stuff data into the other buffers
        private void addDatum(long time, double value, boolean valid) {
            Map<String, String> datum = new HashMap<String, String>();
            datum.put(FeedProvider.NORMALIZED_IS_VALID_KEY, Boolean.toString(valid));
            String status = valid ? " " : "S";
            Color c = valid ? GOOD_COLOR : LOS_COLOR;
            RenderingInfo ri = new RenderingInfo(Double.toString(value), c, status, c, valid);
            ri.setPlottable(valid);
            datum.put(FeedProvider.NORMALIZED_RENDERING_INFO, ri.toString());

            datum.put(FeedProvider.NORMALIZED_TIME_KEY, Long.toString(time));
            datum.put(FeedProvider.NORMALIZED_VALUE_KEY, Double.toString(value));
            datum.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

            // TODO: Buffer data (for a short period) if the archive isn't available
            if(archive != null) {
                try {
                    archive.putData(TelemetryComponent.TelemetryPrefix + "edcec9ef06b844db94be1fefb7380fa4",
                            TimeUnit.MILLISECONDS, time, datum);
                } catch(BufferFullException e) {
                    // TODO: What do we do?
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                }
            }
        }
    }
}
