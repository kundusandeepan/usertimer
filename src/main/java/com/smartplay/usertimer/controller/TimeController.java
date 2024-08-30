package com.smartplay.usertimer.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartplay.usertimer.model.data.UserTimer;
import com.smartplay.usertimer.repository.interfaces.IUserTimerRepository;
import com.smartplay.usertimer.services.impl.TimerService;

@RestController
@RequestMapping("/timers")
public class TimeController {

    @Autowired
    private TimerService timerService;

    @Autowired
    private IUserTimerRepository userTimerRepository;

    @PostMapping("/start")
    public ResponseEntity<String> startTimer(@RequestParam String lpaId) {
        timerService.startTimer(lpaId);
        return ResponseEntity.ok("Timer started for user: " + lpaId);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserTimer>> getAllActiveTimers() {
        List<UserTimer> activeTimers = timerService.getActiveTimers();
        return ResponseEntity.ok(activeTimers);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserTimer>> getAllStoredTimers() {
        Iterable<UserTimer> allTimers = userTimerRepository.findAll();
        List<UserTimer> allTimersList = new ArrayList<>();
        allTimers.forEach(allTimersList::add);
        return ResponseEntity.ok(allTimersList);
    }
}