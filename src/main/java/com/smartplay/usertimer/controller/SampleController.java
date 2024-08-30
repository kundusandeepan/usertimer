package com.smartplay.usertimer.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartplay.usertimer.model.data.TestEntity;
import com.smartplay.usertimer.services.impl.SampleService;

@RestController
@RequestMapping("/api/sample")
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @PostMapping
    public TestEntity create(@RequestBody TestEntity entity) {
        return sampleService.save(entity);
    }

    @GetMapping("/{id}")
    public Optional<TestEntity> getById(@PathVariable String id) {
        return sampleService.findById(id);
    }

    @GetMapping
    public Iterable<TestEntity> getAll() {
        return sampleService.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        sampleService.deleteById(id);
    }
}