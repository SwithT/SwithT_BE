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
public class MemberUpdateDto {

    //수정 페이지
    private String profileImage;
    private String nickName;
    private String name;
    private LocalDate birthday;
    private String gender;
    private String address;
    private String phoneNumber;

}
