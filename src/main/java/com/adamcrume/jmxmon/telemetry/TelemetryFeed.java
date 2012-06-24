package com.adamcrume.jmxmon.telemetry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TelemetryFeed {
    private String telemetryId;

    private String description;

    private String jmxURL;

    private String mbean;

    private String attribute;


    public void setId(String telemetryId) {
        this.telemetryId = telemetryId;
    }


    public String getId() {
        return telemetryId;
    }


    public void setDescription(String telemetryDescription) {
        this.description = telemetryDescription;
    }


    public String getDescription() {
        return description;
    }


    public String getJmxURL() {
        return jmxURL;
    }


    public void setJmxURL(String jmxURL) {
        this.jmxURL = jmxURL;
    }


    public String getMbean() {
        return mbean;
    }


    public void setMbean(String mbean) {
        this.mbean = mbean;
    }


    public String getAttribute() {
        return attribute;
    }


    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
