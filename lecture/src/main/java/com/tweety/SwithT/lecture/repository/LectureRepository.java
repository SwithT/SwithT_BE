package com.tweety.SwithT.lecture.repository;

import com.tweety.SwithT.lecture.domain.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Page<Lecture> findAll(Specification<Lecture> specification, Pageable pageable);


    Optional<Lecture> findByIdAndDelYn(Long id, String delYn);

    @Query("SELECT l FROM Lecture l WHERE l.delYn = 'N' AND l.status = 'ADMIT' ORDER BY l.createdTime DESC")
    List<Lecture> findByDelYnOrderByCreatedTime(Pageable pageable);

    @Query("SELECT l FROM Lecture l JOIN l.lectureGroups lg " +
            "WHERE lg.price = 0 AND lg.isAvailable = 'Y' " +
            "ORDER BY l.createdTime DESC")
    List<Lecture> findLecturesWithAvailableGroups(Pageable pageable);
}
