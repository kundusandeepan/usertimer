package com.smartplay.usertimer.services.interfaces;

import java.util.List;

import com.smartplay.usertimer.model.data.UserTimer;

public interface ITimerService {

    /*
     * Check if the user has an active timer
     */
    boolean hasActiveTimer(String lpaId);
    /*
     * Start and stop the timer for the user
     */
    void startTimer(String lpaId);
    /*
     * Stop the timer for the user
     */
    void stopTimer(String lpaId);

    List<UserTimer> getActiveTimers();
}
