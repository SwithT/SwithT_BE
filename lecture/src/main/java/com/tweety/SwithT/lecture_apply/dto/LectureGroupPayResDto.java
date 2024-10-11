package com.tweety.SwithT.lecture_apply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureGroupPayResDto {
    private Long groupId;
    private String lectureName;
    private int price;
}
