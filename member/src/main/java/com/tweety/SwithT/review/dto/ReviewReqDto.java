package com.tweety.SwithT.review.dto;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.review.domain.Review;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReqDto {

    private Long tutorId;
    @NotNull(message = "평점을 선택해주세요")
    private BigDecimal star;

    @Column(nullable = false)
    private Long applyId;

    @NotNull(message = "제목을 작성해주세요")
//    @Size(min = 3, max = 20, message = "제목은 3글자 이상 15 글자 이하로 작성해야 합니다.")
    private String title;
    @NotNull(message = "내용을 작성해주세요")
//    @Size(min = 1, max = 100, message = "제목은 1글자 이상 100 글자 이하로 작성해야 합니다.")
    private String contents;

    public Review toEntity(Member writerId,Member tutorId) {

        return Review.builder()
                .applyId(this.applyId)
                .tutorId(tutorId)
                .writerId(writerId)
                .profileImage(writerId.getProfileImage())
                .name(writerId.getName())
                .star(this.star)
                .title(this.title)
                .contents(this.contents)
                .build();
    }

}