package com.tweety.SwithT.member.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tweety.SwithT.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    // 연관관계 추가 예정 ERD 확정 시 작업하겠음.



}
