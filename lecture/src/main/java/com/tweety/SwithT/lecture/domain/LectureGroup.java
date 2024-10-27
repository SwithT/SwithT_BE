package com.tweety.SwithT.lecture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.lecture_apply.domain.LectureApply;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LectureGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(nullable = false, columnDefinition = "char(1) default 'Y'")
    private String isAvailable;

    @Column(columnDefinition = "integer default 0")
    private Integer price;

    @Column(columnDefinition = "integer default 1")
    private Integer limitPeople;

    private Integer remaining;  // 남은 자리수

    private String address;
    private String detailAddress;


//    private String latitude;
//
//    private String longitude;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @OneToMany(mappedBy = "lectureGroup", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<GroupTime> groupTimes = new ArrayList<>();

    @OneToMany(mappedBy = "lectureGroup", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<LectureApply> lectureApplies = new ArrayList<>();


    // update - dto로 수정하기
    public void updatePrice(Integer price){
        this.price = price;
    }
    public void updateLimitPeople(Integer limitPeople){
        this.limitPeople = limitPeople;
    }
    public void updateAddress(String address){
        this.address = address;
    }
    public void updateDetailAddress(String detailAddress){this.detailAddress = detailAddress;}
    public void updateDate(LocalDate startDate, LocalDate endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateIsAvailable(String status){
        this.isAvailable = status;
    }

    // 남은 자리수 감소
    public void decreaseRemaining(){
        this.remaining--;
    }
    // 자리 증가
    public void increseRemaining(){
        this.remaining++;
    }

    // 남은 자리수가 0인 경우 신청 종료
    public boolean end(){
        return this.remaining == 0;
    }
}
