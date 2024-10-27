package com.tweety.SwithT.member.dto;

import com.tweety.SwithT.member.domain.Gender;
import com.tweety.SwithT.member.domain.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAddInfoReqDto {

    @NotEmpty(message = "id 값은 반드시 필요합니다.")
    private Long id;
    @NotEmpty(message = "이름은 필수 작성 항목 입니다.")
    private String name;
    @Nullable
    private String introduce;
    @NotNull
    private LocalDate birthday;
//  enum 타입에는 @NotEmpty 적용할 수 없음. 적용 시 에러 발생.
//  @NotEmpty(message = "성별은 필수 항목 입니다.")
    private Gender gender;
    @NotNull
//    @NotEmpty(message = "휴대폰 번호는 필수 작성 항목 입니다.")
//    @Pattern(regexp = "^[0-9]{10,11}$", message = "휴대폰 번호 형식이 유효하지 않습니다.")
    private String phoneNumber;
    @Nullable
    private String education; // 학력
    @Nullable
    private String address;
    @Nullable
    private String detailAddress;
    @Nullable
    private String profileImage;
    @Builder.Default
    @Column(precision = 2, scale = 1, nullable = true)
    private BigDecimal avgScore = BigDecimal.valueOf(0.0);

    private Role role;

}
