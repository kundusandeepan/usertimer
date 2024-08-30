package com.smartplay.usertimer.model.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.smartplay.usertimer.model.data.UserTimer;

import lombok.Getter;

@Getter
public class ResetTimerEvent extends ApplicationEvent {

    private UUID id;
    private String lpaId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @lombok.Builder(builderClassName = "Builder")
    public ResetTimerEvent(Object source, UserTimer userTimer) {
        super(source);
        this.id = userTimer.getId();
        this.lpaId = userTimer.getLpaId();
        this.startTime = userTimer.getStartTime();
        this.endTime = userTimer.getEndTime();
    }

}
