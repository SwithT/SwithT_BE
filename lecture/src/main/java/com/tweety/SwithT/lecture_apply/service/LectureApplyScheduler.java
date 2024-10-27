package com.tweety.SwithT.lecture_apply.service;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import com.tweety.SwithT.lecture_apply.repository.LectureApplyRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LectureApplyScheduler {
    private final LectureApplyRepository lectureApplyRepository;

    //endDate 지난 강의 수강 status를 TERMINATE 변경
    @SchedulerLock(name = "update_status_to_terminate", lockAtLeastFor = "20s", lockAtMostFor = "50s")
    @Scheduled(cron = "0 * * * * *")    //1분마다
//    @Scheduled(cron = "0 0 0 * * *")    //자정
    @Transactional
    public void updateLectureApplyStatusToTerminate() throws InterruptedException {
        List<LectureApply> lectureApplyList = lectureApplyRepository.findByStatusAndDelYn(Status.ADMIT, "N");
        LocalDate today = LocalDate.now();
        System.out.println("tody : " + today);
        for(LectureApply lectureApply : lectureApplyList){
            if(lectureApply.getEndDate().isBefore(today)){
                lectureApply.updateStatus(Status.TERMINATE);
            }
        }


    }

}
