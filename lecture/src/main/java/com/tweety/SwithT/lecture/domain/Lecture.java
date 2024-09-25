package com.tweety.SwithT.lecture.domain;

import com.tweety.SwithT.common.domain.BaseTimeEntity;
import com.tweety.SwithT.common.domain.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Lecture extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String memberName;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;

    private String image;

    // 강의 상태
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private LectureType lectureType;

    // lecture에서 lectureGroup을 접근하기 위한 변수
    // lecture.getLectureGroups() => 리턴타입 List
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.PERSIST)
    // @Builder.Default : 빌더 패턴에서도 ArrayList로 초기화 되도록하는 설정
    @Builder.Default
    private List<LectureGroup> lectureGroups = new ArrayList<>();

//    public LectureResDto listFromEntity() {
//        return LectureResDto;
//    }
}
