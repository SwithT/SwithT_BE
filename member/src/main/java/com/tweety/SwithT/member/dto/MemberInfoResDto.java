package com.tweety.SwithT.member.dto;

import com.tweety.SwithT.member.domain.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetResDto {

    //마이 페이지
    private String profileImage;
    private String nickName;
    private String name;
    private LocalDate birthday;
    private String gender;
    private String address;
    private String phoneNumber;
    //튜터만 보여줄 필드
    private String education;

}
