package com.tweety.SwithT.member.dto;

import com.tweety.SwithT.member.domain.Gender;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.domain.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveReqDto {

    // 소셜 로그인 공급자
    private String provider;
    // 공급자 ID
    private String providerId;

    @NotEmpty(message = "이름은 필수 작성 항목 입니다.")
    private String name;

//    @NotEmpty(message = "닉네임은 필수 작성 항목 입니다")
//    private String nickName;

    @NotEmpty(message = "이메일은 필수 작성 항목 입니다.")
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    private String email;

//    테스트 용이성을 위해 주석처리 나중에 살리겠음
//    @NotEmpty(message = "Password is required")
//    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "패스워드는 특수 문자를 포함한 8자리 이상 입니다.")
//    @Size(min = 8, message = "비밀번호는 8자리 이상이어야합니다")
    private String password;

    @NotNull
    private LocalDate birthday;

//    테스트 용이성을 위해 주석처리 나중에 살리겠음
//    @NotEmpty(message = "휴대폰 번호는 필수 작성 항목 입니다.")
//    @Pattern(regexp = "^[0-9]{10,11}$", message = "휴대폰 번호 형식이 유효하지 않습니다.")
    @NotNull
    private String phoneNumber;

    @Nullable
    private String address;
    @Nullable
    private String detailAddress;

    @Nullable
    private String profileImage;

    @Nullable
    private String introduce;

    @Nullable
    private String education;

    @Builder.Default
    @Column(precision = 2, scale = 1, nullable = true)
    private BigDecimal avgScore = BigDecimal.valueOf(0.0);

//  enum 타입에는 @NotEmpty 적용할 수 없음. 적용 시 에러 발생.
//    @NotEmpty(message = "성별은 필수 항목 입니다.")
    private Gender gender;

    private Role role;

    public Member toEntity(String encodedPassword,String imageUrl) {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .birthday(this.birthday)
                .phoneNumber(this.phoneNumber)
                .address(this.address)
                .detailAddress(this.detailAddress)
                .profileImage(imageUrl) // s3 이미지 경로
                .education(this.education)
                .introduce(this.introduce)
                .gender(this.gender)
                .role(this.role)
                .avgScore(this.avgScore)
                .build();
    }

    // 안쓸거면 지우기
    public Member addInfoToEntity() {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .providerId(this.providerId)
                .provider(this.provider)
                .build();
    }
}
