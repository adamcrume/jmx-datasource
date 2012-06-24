package com.adamcrume.jmxmon.telemetry;

import gov.nasa.arc.mct.services.activity.TimeService;

public class TimeServiceImpl extends TimeService {
    private static final TimeServiceImpl instance = new TimeServiceImpl();


    private TimeServiceImpl() {
    }


    public static TimeServiceImpl getInstance() {
        return instance;
    }


    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
