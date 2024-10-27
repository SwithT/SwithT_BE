package com.tweety.SwithT.lecture_apply.domain;

import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.LectureGroup;
import com.tweety.SwithT.lecture.domain.ReviewStatus;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureApplyListDto;
import com.tweety.SwithT.lecture_apply.dto.SingleLectureTuteeListDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LectureApply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_group_id")
    private LectureGroup lectureGroup;

    @Column(nullable = false)
    private Long memberId;

    //신청한 튜티의 이름
    @Column(nullable = false)
    private String memberName;

    @Column(nullable = true)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    @Column(nullable = true)
    private String location;

    @Column(nullable = true)
    private String detailAddress;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.STANDBY;

    @Builder.Default // 김민성 리뷰 관리를 위해 추가
    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus = ReviewStatus.N;

    public void updateReviewStatus(ReviewStatus updateReviewStatus){

        this.reviewStatus = updateReviewStatus;

    }

    public SingleLectureApplyListDto fromEntityToSingleLectureApplyListDto(){
        return SingleLectureApplyListDto.builder()
                .tuteeName(this.memberName)
                .memberId(this.memberId)
                .applyId(this.id)
                .status(this.status)
                .build();
    }

    public SingleLectureTuteeListDto fromEntityToSingleLectureTuteeListDto(){
        return SingleLectureTuteeListDto.builder()
                .tuteeName(this.memberName)
                .memberId(this.memberId)
                .build();
    }


    public void updateStatus(Status updateStatus){
        this.status = updateStatus;
    }

    public void updatePaidStatus(String updateStatus) {
        switch (updateStatus) {
            case "paid":
                this.status = Status.ADMIT; // 결제 완료 시 ADMIT 상태로 변경
                break;
            case "cancelled":
                this.status = Status.CANCEL; // 결제가 취소된 경우 CANCEL 상태로 변경
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 결제 상태입니다: " + updateStatus);
        }
    }

}
