package com.tweety.SwithT.member.dto;

import com.tweety.SwithT.member.domain.Gender;
import com.tweety.SwithT.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberListResDto {
    //회원 리스트 조회
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String address;
    private BigDecimal avgScore;
    private Gender gender;

}
