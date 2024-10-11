package com.tweety.SwithT.member.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.member.dto.MemberAddInfoReqDto;
import com.tweety.SwithT.member.dto.MemberInfoResDto;
import com.tweety.SwithT.member.dto.MemberUpdateDto;
import com.tweety.SwithT.review.domain.Review;
import com.tweety.SwithT.scheduler.domain.Scheduler;
import com.tweety.SwithT.withdrawal.domain.WithdrawalRequest;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Hibernate 프록시 무시
public class Member extends BaseTimeEntity {

    // nullable을 고려해서 소셜 로그인 정보를 저장해야하기 때문에 DTO단에서 정보 입력을 제한해야할 것 같음. 논의필요

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 공급자
    private String provider;
    // 공급자 ID
    private String providerId;

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    @Builder.Default
    @JsonBackReference
    private List<Scheduler> schedulers = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<WithdrawalRequest> withdrawalRequests = new ArrayList<>();

    // 리뷰 작성자 연관 관계 필드
    @OneToMany(mappedBy = "writerId", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
    // 튜터 연관 관계 필드
    @OneToMany(mappedBy = "tutorId", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Review> tutors = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true) // 동명이인 고려
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = true)
    private LocalDate birthday;

    @Column(nullable = true)  //소셜 로그인때문에 잠시 true
    private String phoneNumber;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String profileImage;

    @Column(nullable = true)
    private String introduce;

    @Column(nullable = true)
    private String education;

    @Builder.Default
    @Column(precision = 2, scale = 1, nullable = true)
    private BigDecimal avgScore = BigDecimal.valueOf(0.0);

    @Builder.Default
    @Column(nullable = true)
    private Long availableMoney = 1000000L; //나중에 0원 셋팅해야함.

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Gender gender = Gender.MAN;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Role role = Role.TUTEE;

    // 내 정보 데이터 FromEntity 메서드
    public MemberInfoResDto infoFromEntity(){
        return MemberInfoResDto.builder()
                .profileImage(this.profileImage)
                .name(this.name)
                .birthday(this.birthday)
                .gender(this.gender)
                .address(this.address)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .education(this.education)
                .introduce(this.introduce)
                .role(this.role)
                .build();
    }

    public Member infoUpdate(MemberUpdateDto dto) {
        this.name = dto.getName();
        this.birthday = dto.getBirthday();
        this.gender = Gender.valueOf(dto.getGender());
        this.address = dto.getAddress();
        this.phoneNumber = dto.getPhoneNumber();
        this.education = dto.getEducation();
        this.introduce = dto.getIntroduce();
        return this;
    }

    public Member addInfoUpdate(MemberAddInfoReqDto memberAddInfoReqDto) {
        this.name = memberAddInfoReqDto.getName();
        this.birthday = memberAddInfoReqDto.getBirthday();
        this.gender = memberAddInfoReqDto.getGender();
        this.address = memberAddInfoReqDto.getAddress();
        this.phoneNumber = memberAddInfoReqDto.getPhoneNumber();
        this.education = memberAddInfoReqDto.getEducation();
        this.introduce = memberAddInfoReqDto.getIntroduce();
        return this;
    }


    public Member imageUpdate(String imgUrl){
        this.profileImage = imgUrl;
        return this;
    }

    public void balanceUpdate(Long amount) {
        this.availableMoney -= amount;
    }

    // avgScore 설정 메서드
    public void setAvgScore(BigDecimal avgScore) {
        if (avgScore != null) {
            this.avgScore = avgScore.setScale(1, BigDecimal.ROUND_HALF_UP); // 소수점 자리 맞추기
        }
    }

    // setEmail 메서드 추가 @Setter 안쓰기 위함.
    public void setEmail(String email) {
        this.email = email;
    }



}
