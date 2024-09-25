package com.tweety.SwithT.member.dto;

import com.tweety.SwithT.member.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoResDto {

    //마이 페이지
    private String profileImage;
    //private String nickName; 닉네임 미사용으로 주석 처리.
    private String name;
    private LocalDate birthday;
    private Gender gender;
    private String address;
    private String email;
    private String phoneNumber;
    //튜터 필드
    private String education;
    //튜터 필드
    private String introduce;

}
