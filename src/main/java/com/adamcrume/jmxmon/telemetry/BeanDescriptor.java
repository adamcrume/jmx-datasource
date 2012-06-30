package com.adamcrume.jmxmon.telemetry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BeanDescriptor {
    private String bean;

    private String attribute;


    public String getBean() {
        return bean;
    }


    public void setBean(String bean) {
        this.bean = bean;
    }


    public String getAttribute() {
        return attribute;
    }


    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
