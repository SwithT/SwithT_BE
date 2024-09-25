package com.tweety.scheduler.repository;

import com.tweety.scheduler.domain.Scheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SchedulerRepository extends JpaRepository<Scheduler,Long> {

}
