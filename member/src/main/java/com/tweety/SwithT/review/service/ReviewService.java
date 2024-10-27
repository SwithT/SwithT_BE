package com.tweety.SwithT.review.service;


import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.repository.MemberRepository;
import com.tweety.SwithT.review.domain.Review;
import com.tweety.SwithT.review.dto.ReviewListResDto;
import com.tweety.SwithT.review.dto.ReviewReqDto;
import com.tweety.SwithT.review.dto.ReviewUpdateDto;
import com.tweety.SwithT.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, MemberRepository memberRepository) {
        this.reviewRepository = reviewRepository;
        this.memberRepository = memberRepository;
    }

    public Review createReview(ReviewReqDto dto) {

        Member writerId = memberRepository
                .findById(Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 정보 입니다."));

        Member tutorId = memberRepository.findById(dto.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 튜터 정보 입니다."));

        Review review = dto.toEntity(writerId,tutorId);
        reviewRepository.save(review);

        return review;
    }

    @Transactional(readOnly = true)
    public Page<ReviewListResDto> getReviewsByTutorId(Long tutorId, Pageable pageable) {
        Member tutor = memberRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 TUTOR 입니다."));

        return reviewRepository.findByTutorId(tutor, pageable)
                .map(Review::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ReviewListResDto> getAllReviewsByTutorId(Long tutorId) {
        // 튜터 ID로 Member 객체를 조회
        Member tutor = memberRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 튜터 정보입니다."));

        // 해당 튜터의 모든 리뷰 조회
        List<Review> reviews = reviewRepository.findAllByTutorId(tutor);

        return reviews.stream()
                .map(Review::fromEntity)
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
//    public Page<ReviewListResDto> getReviews(Pageable pageable) {
//        Page<Review> reviews = reviewRepository.findAll(pageable);
//        return reviews.map(Review::fromEntity);
//    }

    public ReviewUpdateDto updateReview(Long id, ReviewUpdateDto updateDto) {

        Member member = memberRepository
                .findById(Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 정보 입니다."));

        Review review = reviewRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 리뷰 입니다."));

        if ( !review.getWriterId().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.updateReview(updateDto);
        return updateDto;
    }

    public BigDecimal getAverageScoreForTutor(Long tutorId) {
        return reviewRepository.findAverageScoreByTutorId(tutorId);
    }
}
