package com.tweety.SwithT.lecture.service;

import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.domain.LectureType;
import com.tweety.SwithT.lecture.dto.LectureDetailResDto;
import com.tweety.SwithT.lecture.dto.LectureListResDto;
import com.tweety.SwithT.lecture.dto.LectureSearchDto;
import com.tweety.SwithT.lecture.repository.LectureGroupRepository;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
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
                    predicates.add(criteriaBuilder.like(root.get("title"), "%"+searchDto.getSearchTitle()+"%"));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory()+"%"));
                }
                if(searchDto.getLectureType() != null){
                    predicates.add(criteriaBuilder.like(root.get("lectureType"), "%"+searchDto.getLectureType()+"%"));
                }
                if(searchDto.getStatus() != null){
                    predicates.add(criteriaBuilder.like(root.get("status"), "%"+searchDto.getStatus()+"%"));
                }


                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        Page<Lecture> lectures = lectureRepository.findAll(specification, pageable);

        return lectures.map(Lecture::fromEntityToLectureListResDto);
    }


    public Page<LectureListResDto> showMyLectureList(LectureSearchDto searchDto, Pageable pageable) {
        Long memberId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Specification<Lecture> specification = new Specification<Lecture>() {
            @Override
            public Predicate toPredicate(Root<Lecture> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("memberId"), memberId));

                if(searchDto.getSearchTitle() != null){
                    predicates.add(criteriaBuilder.like(root.get("title"), "%"+searchDto.getSearchTitle()+"%"));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory()+"%"));
                }
                if(searchDto.getLectureType() != null){
                    predicates.add(criteriaBuilder.like(root.get("lectureType"), "%"+searchDto.getLectureType()+"%"));
                }
                if(searchDto.getStatus() != null){
                    predicates.add(criteriaBuilder.like(root.get("status"), "%"+searchDto.getStatus()+"%"));
                }


                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        Page<Lecture> lectures = lectureRepository.findAll(specification, pageable);

        return lectures.map(Lecture::fromEntityToLectureListResDto);
    }

    //강의 상세 화면
    public LectureDetailResDto lectureDetail(Long id) {
        Lecture lecture = lectureRepository.findById(id).orElseThrow(()->{
            throw new EntityNotFoundException("해당 id에 맞는 강의가 존재하지 않습니다.");
        });
        return lecture.fromEntityToLectureDetailResDto();
    }
}
