package com.tweety.SwithT.review.service;

import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.domain.Role;
import com.tweety.SwithT.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Component
public class ReviewScheduler {

    private final ReviewService reviewService;
    private final MemberRepository memberRepository;
    @Autowired
    public ReviewScheduler(ReviewService reviewService, MemberRepository memberRepository) {
        this.reviewService = reviewService;
        this.memberRepository = memberRepository;
    }


//    @Scheduled(cron = "*/5 * * * * *") // 5초마다 실행 (필요에 따라 변경)
    @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행
    @Transactional
    public void postSchedule() {
        System.out.println("ReviewScheduler : 스케줄러 시작");

        List<Member> tutors = memberRepository.findAllByRole(Role.TUTOR); // 튜터 리스트 가져오기

        for (Member tutor : tutors) {
            BigDecimal avgScore = reviewService.getAverageScoreForTutor(tutor.getId()); // 평균 점수 가져오기

            if (avgScore != null) {
                tutor.setAvgScore(avgScore); // 평균 점수 업데이트
                System.out.printf("튜터 ID: %d, 새로운 평균 점수: %s로 업데이트되었습니다.%n", tutor.getId(), avgScore);
            } else {
                System.out.printf("튜터 ID: %d의 평균 점수 계산에 실패했습니다.%n", tutor.getId());
            }
        }
    }

}
