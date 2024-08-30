package com.smartplay.usertimer.services.interfaces;

public interface INotificationService {
    <T> void publishEvent(T event);
}
