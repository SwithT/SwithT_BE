package com.tweety.SwithT.lecture.dto;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.LectureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureSearchDto {
    private String searchTitle;
    private String category;
    private String status;
    private String lectureType;
}
