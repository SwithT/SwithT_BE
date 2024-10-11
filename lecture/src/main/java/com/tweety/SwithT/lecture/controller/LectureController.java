package com.tweety.SwithT.lecture.controller;

import com.tweety.SwithT.common.dto.CommonErrorDto;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.dto.*;
import com.tweety.SwithT.lecture.service.LectureService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    // 강의 Or 과외 생성
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/create")
    public ResponseEntity<Object> lectureCreate(
            @RequestPart(value = "data") CreateReqDto lectureCreateDto,
            @RequestPart(value = "file", required = false) MultipartFile imgFile) {
        Lecture lecture = lectureService.lectureCreate(lectureCreateDto.getLectureReqDto(), lectureCreateDto.getLectureGroupReqDtos(), imgFile);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Lecture is successfully created", lecture.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //과외 또는 강의 리스트
    @GetMapping("/list-of-lecture")
    public ResponseEntity<?> showLectureList(@ModelAttribute LectureSearchDto searchDto, Pageable pageable) {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 리스트", lectureService.showLectureList(searchDto, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //튜터 자신의 과외/강의 리스트
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/my-lecture-list")
    public ResponseEntity<?> showMyLectureList(@ModelAttribute LectureSearchDto searchDto, Pageable pageable) {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "나의 강의 리스트", lectureService.showMyLectureList(searchDto, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //과외/강의 상세화면
    @GetMapping("/lecture-detail/{id}")
    public ResponseEntity<?> lectureDetail(@PathVariable Long id) {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 안내 화면", lectureService.lectureDetail(id));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //과외/관리 수업 관리 화면
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/lecture-class-list/{id}")
    public ResponseEntity<?> showLectureGroupList(@PathVariable Long id, @RequestParam(value = "isAvailable") String isAvailable, Pageable pageable) {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의/과외 수업 리스트 화면", lectureService.showLectureGroupList(id, isAvailable, pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    //강의 검색 API (OpenSearch 사용)
    @PostMapping("/lecture/search")
    public ResponseEntity<?> searchLectures(
            @RequestBody LectureSearchDto searchDto,  // JSON으로 검색 조건 받기
            Pageable pageable,
            PagedResourcesAssembler<LectureListResDto> assembler) {
        try {
            // 검색 수행 후 Page 객체로 반환
            Page<LectureListResDto> searchResults = lectureService.showLectureListInOpenSearch(searchDto, pageable);

            // PagedModel로 변환 (LectureListResDto를 EntityModel로 감싸기)
            PagedModel<EntityModel<LectureListResDto>> pagedModel = assembler.toModel(searchResults,
                    lecture -> EntityModel.of(lecture) // LectureListResDto를 EntityModel로 변환
            );

            // 필요한 데이터만 추출하여 응답 구조를 생성
            Map<String, Object> result = new HashMap<>();
            result.put("content", pagedModel.getContent()); // 실제 데이터
            result.put("page", Map.of(
                    "size", searchResults.getSize(),
                    "totalElements", searchResults.getTotalElements(),
                    "totalPages", searchResults.getTotalPages(),
                    "number", searchResults.getNumber()
            )); // 페이지네이션 정보

            // 검색 결과 응답
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 검색 결과", result);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // 검색 중 오류 발생 시 처리
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "검색 중 오류 발생: " + e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 강의 상태 업데이트
    @PutMapping("/lectures/{id}/status")
    public ResponseEntity<?> updateLectureStatus(@PathVariable Long id, @RequestBody LectureStatusUpdateDto statusUpdateDto) {
        try {
            lectureService.updateLectureStatus(statusUpdateDto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 상태가 성공적으로 변경되었습니다", null);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "강의 정보를 불러오는 데 실패했습니다.");
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    // 강의 수정
    @PostMapping("/update/{id}")
    public ResponseEntity<?> lectureUpdate(@PathVariable Long id, @RequestBody LectureUpdateReqDto dto) {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 업데이트", lectureService.lectureUpdate(id, dto));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 강의 삭제
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> lectureDelete(@PathVariable Long id) {
        lectureService.lectureDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 삭제", id);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 강의 그룹 수정
    @PostMapping("/update/lecture-group/{id}")
    public ResponseEntity<?> lectureGroupUpdate(@PathVariable Long id, @RequestBody LectureGroupReqDto dto) {
        lectureService.lectureGroupUpdate(id, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 그룹 업데이트", id);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 강의 그룹 삭제
    @PostMapping("/delete/lecture-group/{id}")
    public ResponseEntity<?> lectureGroupDelete(@PathVariable Long id) {
        lectureService.lectureGroupDelete(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 그룹 삭제", id);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 최신순 강의 4개 조회
    @GetMapping("/lectures/latest")
    public ResponseEntity<?> getLatestLectures() {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 조회", lectureService.getLatestLectures());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/lectures/free")
    public ResponseEntity<?> getFreeLectures() {
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 조회", lectureService.getFreeLectures());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    // 강의 홈 정보 - id : lecture group id
    @GetMapping("/lecture-group-home/{id}")
    public  ResponseEntity<?> lectureHomeInfoGet(@PathVariable("id") Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "강의 홈 정보", lectureService.LectureHomeInfoGet(id));
        return new ResponseEntity<>(commonResDto,HttpStatus.OK);
    }

    // 강의 제목, 썸네일 가져오는 요청
    @GetMapping("lecture/get-image-and-title/{id}")
    public ResponseEntity<?> getImageAndThumbnail(@PathVariable Long id){
        LectureTitleAndImageResDto dto = lectureService.getTitleAndThumbnail(id);
        try {
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "제목과 썸네일", dto);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }
}
