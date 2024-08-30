package com.smartplay.usertimer.services.impl;

import org.springframework.stereotype.Service;

import com.smartplay.usertimer.services.interfaces.IConfigurationService;

@Service
public class ConfigurationService implements IConfigurationService {

    public static final long TIMER_WINDOW_IN_SECONDS = 4L * 60 * 60 ; // 4 hours

    @Override
    public long getTimerDuration() {
        return TIMER_WINDOW_IN_SECONDS;
    }

}
