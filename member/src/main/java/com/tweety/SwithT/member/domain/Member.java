package com.tweety.SwithT.member.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.member.dto.MemberInfoResDto;
import com.tweety.SwithT.member.dto.MemberUpdateDto;
import com.tweety.scheduler.domain.Scheduler;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true) //동명이인 고려
    private String name;

//    닉네임 주석 처리
//    @Column(nullable = false, unique = true)
//    private String nickName;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String profileImage;

    //튜터 컬럼
    @Column(nullable = true)
    private String education;

    //튜터 컬럼
    @Column(precision = 2, scale = 1, nullable = true)
    private BigDecimal avgScore = BigDecimal.valueOf(0.0);

    //튜터 컬럼
    @Column(nullable = true)
    Long availableMoney = 0L;

    // default MAN으로 설정
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Gender gender = Gender.MAN;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Role role = Role.TUTEE;


    // schedulers 연관관계
    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Scheduler> schedulers;


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
                .introduce(this.introduce) // 앞단에서 튜터만 보여주기
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

    public Member imageUpdate(String imgUrl){
        this.profileImage = imgUrl;
        return this;
    }

}
