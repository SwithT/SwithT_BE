//package com.tweety.SwithT.lecture_apply.service;
//
//import com.tweety.SwithT.lecture.domain.LectureGroup;
//import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
//import com.tweety.SwithT.lecture_apply.repository.LectureApplyRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//class LectureApplyServiceTest {
//
//    @Autowired
//    private LectureApplyService lectureApplyService;
//
//    @Autowired
//    private LectureGroupRepository lectureGroupRepository;
//
//    @Autowired
//    private LectureApplyRepository lectureApplyRepository;
//
//    private LectureGroup lectureGroup;
//
//    @BeforeEach
//    void setup() {
//        // 강의 그룹 생성 및 저장
//        lectureGroup = LectureGroup.builder()
//                .limitPeople(100) // 최대 신청 가능 수
//                .remaining(100)   // 초기 남은 자리 수
//                .isAvailable("Y")
//                .build();
//        lectureGroup = lectureGroupRepository.save(lectureGroup);
//    }
//
//    @Test
//    void 강의신청_200명_중_100명_신청_테스트() throws InterruptedException {
////        final Long lectureGroupId = lectureGroup.getId(); // 테스트에 사용할 강의 그룹 ID
//        final Long lectureGroupId = 44L; // 테스트에 사용할 강의 그룹 ID
//        final int totalApplicants = 200; // 총 신청자 수
//        final int limitCount = 100; // 최대 신청 가능 수
//
//        CountDownLatch countDownLatch = new CountDownLatch(totalApplicants);
//        ExecutorService executorService = Executors.newFixedThreadPool(limitCount);
//
//
////        // 강의 그룹 생성 및 저장
////        LectureGroup lectureGroup = new LectureGroup();
////        lectureGroup.setLimitPeople(limitCount);
////        lectureGroup.setRemaining(limitCount);
////        lectureGroup.setIsAvailable("Y");
////        lectureGroupRepository.save(lectureGroup);
//
////        for (int i = 0; i < limitCount; i++) {
////            final long memberId = i + 1; // 고유한 유저 ID 생성
////            executorService.submit(() -> {
////                try {
////                    lectureApplyService.tuteeLectureApply(lectureGroupId, memberId, "User" + memberId);
////                } catch (Exception e) {
////                    System.out.println("Error applying for lecture:" + e.getMessage());
////                } finally {
////                    countDownLatch.countDown();
////                }
////            });
////        }
////
////        countDownLatch.await();
////        executorService.shutdown();
//        List<Thread> workers = Stream.generate(() -> new Thread(new LectureApplyWorker(lectureApplyService, countDownLatch, lectureGroupId)))
//                .limit(totalApplicants)
//                .collect(Collectors.toList());
//
//        workers.forEach(Thread::start);
//        countDownLatch.await();
//
////        // 강의 신청 결과 확인
//        final long successfulApplications = lectureApplyRepository.countByLectureGroupId(lectureGroupId);
//        assertEquals(limitCount, successfulApplications); // 최대 신청 수와 같아야 함
//    }
//
//    private static class LectureApplyWorker implements Runnable {
//        private final LectureApplyService lectureApplyService;
//        private final CountDownLatch countDownLatch;
//        private final Long lectureGroupId;
//
//        public LectureApplyWorker(LectureApplyService lectureApplyService, CountDownLatch countDownLatch, Long lectureGroupId) {
//            this.lectureApplyService = lectureApplyService;
//            this.countDownLatch = countDownLatch;
//            this.lectureGroupId = lectureGroupId;
//        }
//
//        @Override
//        public void run() {
//            try {
//                lectureApplyService.tuteeLectureApply(lectureGroupId, Thread.currentThread().getId(), "User" + Thread.currentThread().getId());
//            } catch (Exception e) {
//                // 예외 처리 로직 (예: 로그 출력)
//            } finally {
//                countDownLatch.countDown();
//            }
//        }
//    }
//}
