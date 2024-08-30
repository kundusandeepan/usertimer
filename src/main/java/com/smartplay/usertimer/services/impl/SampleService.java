package com.smartplay.usertimer.services.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartplay.usertimer.model.data.TestEntity;
import com.smartplay.usertimer.repository.interfaces.SampleRepository;

@Service
public class SampleService {

    @Autowired
    private SampleRepository sampleRepository;

    public TestEntity save(TestEntity entity) {
        return sampleRepository.save(entity);
    }

    public Optional<TestEntity> findById(String id) {
        return sampleRepository.findById(id);
    }

    public Iterable<TestEntity> findAll() {
        return sampleRepository.findAll();
    }

    public void deleteById(String id) {
        sampleRepository.deleteById(id);
    }
}