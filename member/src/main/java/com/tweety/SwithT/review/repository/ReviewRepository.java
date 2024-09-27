package com.tweety.SwithT.review.repository;

import com.tweety.SwithT.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAll(Pageable pageable);

    @Query("SELECT AVG(r.star) FROM Review r WHERE r.tutorId.id = :tutorId")
    BigDecimal findAverageStarByTutorId(@Param("tutorId") Long tutorId);

}
