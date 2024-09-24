package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.GroupTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupTimeRepository extends JpaRepository<GroupTime, Long> {

}
