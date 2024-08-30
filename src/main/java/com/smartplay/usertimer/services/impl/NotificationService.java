package com.smartplay.usertimer.services.impl;

import org.springframework.stereotype.Service;

import com.smartplay.usertimer.services.interfaces.INotificationService;

@Service
public class NotificationService implements INotificationService {

    @Override
    public <T> void publishEvent(T event) {
        // TODO : we can call an api to apiservices to prcoess user's event or publish to SQS

        System.out.println("Event published: " + event);
        // throw new UnsupportedOperationException("Unimplemented method 'publishEvent'");
    }

}
