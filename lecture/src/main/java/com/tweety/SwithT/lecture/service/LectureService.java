package com.tweety.SwithT.lecture.service;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureType;
import com.tweety.SwithT.lecture.dto.LectureListResDto;
import com.tweety.SwithT.lecture.dto.LectureSearchDto;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final LectureGroupRepository lectureGroupRepository;

    public LectureService(LectureRepository lectureRepository, LectureGroupRepository lectureGroupRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureGroupRepository = lectureGroupRepository;
    }

    public Page<LectureListResDto> showLectureList(LectureSearchDto searchDto, Pageable pageable) {
        Specification<Lecture> specification = new Specification<Lecture>() {
            @Override
            public Predicate toPredicate(Root<Lecture> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getSearchTitle() != null){
                    System.out.println("이거 왜 안나오는데2\n\n\n\n\n\n\n\n\n\n");
                    predicates.add(criteriaBuilder.like(root.get("title"), "%"+searchDto.getSearchTitle()+"%"));
                }
                if(searchDto.getCategory() != null){
                    System.out.println("이거 왜 안나오는데3\n\n\n\n\n\n\n\n\n\n");
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory()+"%"));
                }
                if(searchDto.getLectureType() != null){
                    System.out.println("이거 왜 안나오는데4\n\n\n\n\n\n\n\n\n\n");
                    predicates.add(criteriaBuilder.like(root.get("lectureType"), "%"+searchDto.getLectureType()+"%"));
                }
                if(searchDto.getStatus() != null){
                    System.out.println("이거 왜 안나오는데3\n\n\n\n\n\n\n\n\n\n");
                    predicates.add(criteriaBuilder.like(root.get("status"), "%"+searchDto.getStatus()+"%"));
                }


                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        System.out.println("이거 왜 안나오는데4\n\n\n\n\n\n\n\n\n\n");

        Page<Lecture> lectures = lectureRepository.findAll(specification, pageable);
        System.out.println("이거 왜 안나오는데5\n\n\n\n\n\n\n\n\n\n");



        return lectures.map(Lecture::fromEntityToLectureListResDto);
    }


}
