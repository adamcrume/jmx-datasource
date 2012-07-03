package com.adamcrume.jmxmon.telemetry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TelemetryFeed {
    private String telemetryId;

    private String description;


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
}
