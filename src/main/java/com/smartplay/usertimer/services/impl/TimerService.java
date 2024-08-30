package com.smartplay.usertimer.services.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartplay.usertimer.model.data.UserTimer;
import com.smartplay.usertimer.model.event.ResetTimerEvent;
import com.smartplay.usertimer.repository.interfaces.IUserTimerRepository;
import com.smartplay.usertimer.services.interfaces.IConfigurationService;
import com.smartplay.usertimer.services.interfaces.INotificationService;
import com.smartplay.usertimer.services.interfaces.ISystemService;
import com.smartplay.usertimer.services.interfaces.ITimerService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * The TimerService class is responsible for managing timers for users.
 * It provides functionality to start, stop, and schedule timers for a given
 * user.
 * Timers are stored via repository and can be retrieved and updated as needed.
 * The TimerService class also handles timer expiration by triggering completion
 * actions and publishing events.
 */
@Service
@Slf4j
public class TimerService implements ITimerService {

    private final ConcurrentHashMap<String, ScheduledFuture<?>> userTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final IUserTimerRepository userTimerRepository;
    private final ISystemService systemService;
    private final IConfigurationService configurationService;
    private final INotificationService notificationService;

    public TimerService(
            @Autowired IUserTimerRepository userTimerRepository,
            @Autowired IConfigurationService configurationService,
            @Autowired INotificationService notificationService,
            @Autowired ISystemService systemService) {
        this.userTimerRepository = userTimerRepository;
        this.configurationService = configurationService;
        this.notificationService = notificationService;
        this.systemService = systemService;
    }

    /*
     * The @PostConstruct annotation is used on a method that needs to be executed
     * after dependency injection is done to perform any initialization.
     * The init() method is called after the TimerService bean is created and all
     * dependencies are injected.
     * The method loads existing timers from the repository and schedules them for
     * execution.
     * The timers are scheduled based on the time difference between the current
     * time and the expiry time.
     * If the expiry time is in the future, the timer is scheduled to trigger the
     * completion action.
     * If the expiry time is in the past, the completion action is triggered
     * immediately, and a new timer is scheduled for the next available window.
     * The getNextUserTimer() method is used to calculate the next available timer
     * window based on the current time and the original timer's start and end
     * times.
     * The scheduleTimer() method is used to schedule a timer for a given user with
     * a specified delay in seconds.
     * The onTimerCompletionAction() method is called when a timer expires to
     * trigger the completion action and remove the timer from the list of active
     * timers.
     * The triggerCompletionAction() publishes a ResetTimerEvent to notify other
     * components.
     */
    @PostConstruct
    public void init() {
        // Load existing timers from the repository and schedule them
        Iterable<UserTimer> timers = userTimerRepository.findAll();
        for (UserTimer timer : timers) {
            LocalDateTime currentDateTime = systemService.getCurrentDateTime();
            long delay = currentDateTime.until(timer.getEndTime(), TimeUnit.SECONDS.toChronoUnit());
            if (delay > 0) {
                // a. delay positive because the expiry time is in the future.
                CompletableFuture.runAsync(() -> scheduleTimer(timer, delay));
            } else {
                // b. delay is negative because the expiry time is in the past.
                // c. delay is zero because the expiry time is the same as the current time.
                CompletableFuture<Void> completionAction = CompletableFuture.runAsync(() -> triggerCompletionAction(timer));

                CompletableFuture<Void> createNextTimerAndSchedule = CompletableFuture.runAsync(() -> {
                    // Calculate the next available timer window
                    UserTimer nextTimer = getNextUserTimer(timer, currentDateTime);

                    long intervalInSeconds = ChronoUnit.SECONDS.between(currentDateTime, timer.getEndTime());

                    timer.setStartTime(nextTimer.getStartTime());
                    timer.setEndTime(nextTimer.getEndTime());

                    userTimerRepository.save(timer);
                    log(nextTimer, "[Action: new timercreated, saved]");
                    scheduleTimer(timer, intervalInSeconds);
                });
                CompletableFuture.allOf(completionAction, createNextTimerAndSchedule).thenRunAsync(() -> {
                    log(timer, "[Action: @postConstruct] Timer Completed, action triggered, new timer scheduled");
                });
            }
        }
    }

    /*
     * The onTimerCompletionAction() method is called when a timer expires to
     * trigger the completion action for the user.
     * The method first triggers the completion action for the user by calling the
     * triggerCompletionAction() method.
     * It then removes the user from the userTimers map and deletes the timer from
     * the repository.
     */
    private void onTimerCompletionAction(UserTimer userTimer) {
        String lpaId = userTimer.getLpaId();
        //step 1
        CompletableFuture<Void> completionAction = CompletableFuture.runAsync(() -> triggerCompletionAction(userTimer));

        // Combine step 2a and 2b into one CompletableFuture
        CompletableFuture<Void> removeAndDeleteTimer = CompletableFuture.runAsync(() -> {
            userTimers.remove(lpaId);
            userTimerRepository.deleteById(lpaId);
            log(userTimer, "[Action: removeAndDeleteTimer]");
        });

        // Wait for both completion actions to finish
        CompletableFuture.allOf(completionAction, removeAndDeleteTimer).thenRunAsync(() -> {
            // Start a new timer asynchronously
            log(userTimer, "[Action: initiating new timer]");
            startTimer(lpaId);
        });
    }

    /*
     * The @PreDestroy annotation is used on methods as a callback notification to
     * signal that the instance is in the process of being removed by the container.
     * The shutdown() method is called when the TimerService bean is being destroyed
     * to shut down the scheduler.
     * This ensures that all scheduled tasks are completed before the bean is
     * destroyed.
     * The shutdown() method is called when the application is shutting down to
     * clean up resources and stop the scheduler.
     * The scheduler.shutdown() method is used to shut down the scheduler and stop
     * all scheduled tasks.
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    /**
     * Checks if there is an active timer for the given lpaId.
     *
     * @param lpaId the ID of the user
     * @return true if there is an active timer, false otherwise
     */
    @Override
    public boolean hasActiveTimer(String lpaId) {
        return userTimers.containsKey(lpaId);
    }

    /*
     * The startTimer() method is used to start a timer for a given user with a
     * specified duration in seconds.
     * The method first stops any existing timer for the user to avoid multiple
     * timers running concurrently.
     * It then calculates the expiry time by adding the duration to the current
     * time.
     * A new UserTimer object is created with the user ID and expiry time, and it is
     * saved to the repository.
     * The scheduleTimer() method is called to schedule the timer for the user with
     * the specified duration.
     */
    @Override
    public void startTimer(String lpaId) {
        // Check if an active timer is already running for the user
        if (this.hasActiveTimer(lpaId)) {
            log.info("An active timer is already running for user: " + lpaId);
            return;
        }

        // Stop any existing timer for the user
        // this scenario shouldnt happen
        // stopTimer(lpaId);

        // create new Timer
        UserTimer userTimer = createUserTimer(lpaId);

        // Save the timer to the repository
        userTimerRepository.save(userTimer);
        log(userTimer, "[Action: new timercreated, saved]");
        // Schedule the timer
        scheduleTimer(userTimer, userTimer.getDuration());
    }

    /*
     * The stopTimer() method is used to stop a timer for a given user.
     * The method first retrieves the scheduled future for the user from the
     * userTimers map.
     * If a future exists, it is canceled to stop the timer from triggering the
     * completion action.
     * The user is then removed from the userTimers map, and the timer is deleted
     * from the repository.
     */
    @Override
    public void stopTimer(String lpaId) {
        ScheduledFuture<?> future = userTimers.remove(lpaId);
        if (future != null) {
            future.cancel(true);
        }
        userTimerRepository.deleteById(lpaId);
    }

    @Override
    public List<UserTimer> getActiveTimers() {
        LocalDateTime now = LocalDateTime.now();
        return userTimers.keySet().stream()
                .map(lpaId -> userTimerRepository.findById(lpaId).orElse(null))
                .filter(timer -> timer != null && timer.getEndTime().isAfter(now))
                .collect(Collectors.toList());
    }

    /*
     * The getNextUserTimer() method calculates the next available timer window
     * based on the current time and the original timer's start and end times.
     * The method takes a UserTimer object and the current time as input and returns
     * the updated UserTimer object with the next available timer window.
     * If the current time is within the original timer's start and end times, the
     * method returns the original timer as it is.
     * If the current time is after the end time, the method calculates how much
     * time has passed since the end time.
     */
    public static UserTimer getNextUserTimer(UserTimer timer, LocalDateTime currentTime) {
        LocalDateTime startTime = timer.getStartTime();
        LocalDateTime endTime = timer.getEndTime();

        // Calculate the duration between start and end time, which is 4 hours (by
        // design)
        long duration = ChronoUnit.HOURS.between(startTime, endTime);

        // If the current time is within the start and end time range, return them as
        // they are
        if (!currentTime.isAfter(endTime)) {
            return timer;
        }

        // Calculate how much time has passed since the end time
        long hoursAfterStart = ChronoUnit.HOURS.between(endTime, currentTime);

        // Determine the integral multiplier of the duration
        long cycles = hoursAfterStart / duration;

        // If the current time is greater than the new start time, add 1 to cycles
        if (currentTime.isAfter(startTime.plusHours(cycles * duration))) {
            cycles++;
        }
        if (currentTime.equals(startTime.plusHours(cycles * duration).plusHours(duration))) {
            cycles++;
        }

        // Calculate new start and end times by adding the time after the end to the
        // original times
        LocalDateTime newStartTime = startTime.plusHours(cycles * duration);
        LocalDateTime newEndTime = newStartTime.plusHours(duration);

        // Return the updated UserTimer
        return UserTimer.builder()
                .timerId(timer.getTimerId())
                .lpaId(timer.getLpaId())
                .startTime(newStartTime)
                .endTime(newEndTime)
                .build();
    }

    private UserTimer createUserTimer(String lpaId) {
        // Get the duration for the timer
        long durationInSeconds = configurationService.getTimerDuration();
        LocalDateTime startDateTime = systemService.getCurrentDateTime();
        // Calculate the expiry time
        LocalDateTime endTime = startDateTime.plusSeconds(durationInSeconds);
        // Create a new UserTimer object
        return UserTimer.builder()
                .timerId(UUID.randomUUID())
                .lpaId(lpaId)
                .startTime(startDateTime)
                .duration(durationInSeconds)
                .endTime(endTime)
                .build();
    }

    /*
     * The scheduleTimer() method is used to schedule a timer for a given user with
     * a specified delay in seconds.
     * The method creates a new scheduled future using the scheduler to execute the
     * onTimerCompletionAction() method after the specified delay.
     * The future is stored in the userTimers map with the user ID as the key for
     * future reference.
     * When the timer completes, the onTimerCompletionAction() method is called to
     * trigger the completion action for the user.
     */
    private void scheduleTimer(UserTimer userTimer, long completionDelay) {
        String lpaId = userTimer.getLpaId();
        ScheduledFuture<?> future = scheduler.schedule(() -> onTimerCompletionAction(userTimer), completionDelay,
                TimeUnit.SECONDS);
        userTimers.put(lpaId, future);
        log(userTimer, "[Action: timer scheduled] Timer expired");
    }

    





    /*
     * The triggerCompletionAction() method is used to trigger the completion action
     * for a given user.
     * The method publishes a ResetTimerEvent to notify other components.
     */
    private void triggerCompletionAction(UserTimer userTimer) {
        // String lpaId = userTimer.getLpaId();
        log(userTimer, "[Action: triggerCompletionAction]");
        //triggerCompletionAction
        CompletableFuture.runAsync(() -> {
            var event = ResetTimerEvent.builder()
            .userTimer(userTimer)
            .source(this)
            .build();
            notificationService.publishEvent(event);
            log(userTimer, "[Action: event published for reset nottification]");
        });
    }

    private void log(String message) {
        System.out.println(message);
        log.info(message);
    }

    private void log(UserTimer userTimer, String actionMessage) {
        log("LPAID: " + userTimer.getLpaId() + ", Timer: " + userTimer.getTimerId() + ", Start: " + userTimer.getStartTime() + ", End: " + userTimer.getEndTime() + "::" + actionMessage);
    }

}
