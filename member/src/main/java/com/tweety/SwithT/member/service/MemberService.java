package com.tweety.SwithT.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.common.domain.Status;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.common.service.RedisService;
import com.tweety.SwithT.common.service.S3Service;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.dto.*;
import com.tweety.SwithT.member.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
public class MemberService {

    private final RedisService redisService;
    private static final String AUTH_EMAIL_PREFIX = "EMAIL_CERTIFICATE : ";
    private final S3Service s3Service;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final LectureFeign lectureFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MemberService(RedisService redisService, MemberRepository memberRepository, S3Service s3Service, PasswordEncoder passwordEncoder, LectureFeign lectureFeign, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.memberRepository = memberRepository;
        this.s3Service = s3Service;
        this.passwordEncoder = passwordEncoder;
        this.lectureFeign = lectureFeign;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Member login(MemberLoginDto dto){

        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(
                ()-> new EntityNotFoundException("해당 이메일은 존재하지 않습니다"));

        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public Member addInfoUpdate(MemberAddInfoReqDto memberAddInfoReqDto){

        Member member = memberRepository.findById(Long.valueOf(memberAddInfoReqDto.getId()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 입니다."));

        return member.addInfoUpdate(memberAddInfoReqDto);
    }

    public Member memberCreate(MemberSaveReqDto memberSaveReqDto,MultipartFile imgFile) {

        // 레디스에 인증이 된 상태인지 확인
        System.out.println(memberSaveReqDto.getAddress()+"서비스단 address");

        String chkVerified = redisService.getValues(AUTH_EMAIL_PREFIX + memberSaveReqDto.getEmail());

        if (chkVerified == null || !chkVerified.equals("true")) {
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }

        memberRepository.findByEmail(memberSaveReqDto.getEmail()).ifPresent(existingMember -> {
            throw new EntityExistsException("이미 존재하는 이메일입니다.");
        });

        String encodedPassword = passwordEncoder.encode(memberSaveReqDto.getPassword());
        String imageUrl = s3Service.uploadFile(imgFile, "member");

        return memberRepository.save(memberSaveReqDto.toEntity(encodedPassword, imageUrl));
    }

    // 내 이미지 수정
    public Member infoImageUpdate(MultipartFile imgfile){

        String id = tokenCheck();
        Member member = memberRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 입니다."));

        String imgUrl = s3Service.uploadFile(imgfile, "member");
        member.imageUpdate(imgUrl);

        return memberRepository.save(member);
    }

    //내 정보 조회
    public MemberInfoResDto infoGet(){

        String id = tokenCheck();
        Member member = memberRepository.findById(Long.valueOf(id)).orElseThrow(EntityNotFoundException::new);
        return member.infoFromEntity();
    }

    public Member infoUpdate(MemberUpdateDto memberUpdateDto){
        String id = tokenCheck();
        Member member = memberRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 입니다."));

        return member.infoUpdate(memberUpdateDto);

    }

    public void subtraction(Long amount){
        String id = tokenCheck();
        Member member = memberRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 입니다."));
        System.out.println("sub 메서드->"+id);
        member.balanceUpdate(amount);

    }

    // 토큰에서 id 추출 후 체크하는 메서드  (subject)
    public String tokenCheck(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    //회원 이름 가져오는 메서드(feignClient에서 사용)
    public MemberNameResDto memberNameGet(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(()->{
            throw new EntityExistsException("존재하지 않는 회원입니다.");
        });

        return MemberNameResDto.builder()
                .name(member.getName())
                .build();
    }

    //회원 프로필 이미지 가져오는 메서드(feignClient에서 사용)
    public MemberProfileResDto memberProfileGet(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(()->{
            throw new EntityExistsException("존재하지 않는 회원입니다.");
        });

        return MemberProfileResDto.builder()
                .image(member.getProfileImage())
                .build();
    }

//    public void lectureStatusUpdate(Long lectureId, Status newStatus){
//        LectureStatusUpdateDto statusUpdateDto = LectureStatusUpdateDto.builder()
//                .lectureId(lectureId)
//                .status(newStatus)
//                .build();
//
//        lectureFeign.updateLectureStatus(lectureId, statusUpdateDto);
//
//        // Kafka 메시지 전송
//        try {
//            // LectureStatusUpdateDto를 JSON 문자열로 변환
//            String message = objectMapper.writeValueAsString(statusUpdateDto);
//
//            // Kafka로 메시지 전송
//            kafkaTemplate.send("lecture-status-update", message);
//
//            System.out.println("Kafka 메시지 전송됨: " + message);
//        } catch (JsonProcessingException e) {
//            System.err.println("Kafka 메시지 전송 실패: " + e.getMessage());
//        }
//    }

    public void lectureStatusUpdate(Long lectureId, Status newStatus) {
        // Enum인 Status를 String으로 변환하여 LectureStatusUpdateDto 생성
        LectureStatusUpdateDto statusUpdateDto = LectureStatusUpdateDto.builder()
                .lectureId(lectureId)
                .status(newStatus.name())  // Enum Status를 String으로 변환
                .build();

        // Kafka 메시지 전송
        try {
            String message = objectMapper.writeValueAsString(statusUpdateDto);

            kafkaTemplate.send("lecture-status-update", message);  // JSON 문자열 전송

            System.out.println("Kafka 메시지 전송됨: " + message);
        } catch (JsonProcessingException e) {
            System.err.println("Kafka 메시지 변환 및 전송 실패: " + e.getMessage());
        }
    }

//  강의 정보를 가져옴
    private CommonResDto getLectureInfo(Long lecturePayId) {

        return lectureFeign.getLectureById(lecturePayId);
    }
}
