package com.adamcrume.jmxmon.telemetry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JVM {
    private String jmxURL;


    public String getJmxURL() {
        return jmxURL;
    }


    public void setJmxURL(String jmxURL) {
        this.jmxURL = jmxURL;
    }
}
