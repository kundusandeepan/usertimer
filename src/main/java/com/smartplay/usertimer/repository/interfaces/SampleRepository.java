package com.smartplay.usertimer.repository.interfaces;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import com.smartplay.usertimer.model.data.TestEntity;

@EnableScan
public interface SampleRepository extends CrudRepository<TestEntity, String> {
}