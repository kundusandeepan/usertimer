package com.smartplay.usertimer.services.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.smartplay.usertimer.services.interfaces.ISystemService;

@Service
public class SystemService implements ISystemService {
    
    @Override
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
}
