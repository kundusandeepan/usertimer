package com.smartplay.usertimer.repositories.interfaces;

import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.smartplay.usertimer.model.data.UserTimer;

@EnableScan
@Repository
public interface IUserTimerRepository extends CrudRepository<UserTimer, String> {

    Optional<UserTimer> findTopByLpaIdOrderByStartTimeDesc(String lpaId);

    void deleteByLpaId(String lpaId);
}