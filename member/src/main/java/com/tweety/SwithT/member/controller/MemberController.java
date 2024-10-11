package com.tweety.SwithT.member.controller;

import com.tweety.SwithT.common.auth.JwtTokenProvider;
import com.tweety.SwithT.common.dto.CommonErrorDto;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.dto.*;
import com.tweety.SwithT.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class MemberController {

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MemberController(MemberService memberService
            , JwtTokenProvider jwtTokenProvider
            , @Qualifier("2") RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/member/create")
    public ResponseEntity<?> memberCreate(
            @Valid
            @RequestPart(value = "data" ) MemberSaveReqDto dto,
            @RequestPart(value = "file", required = false) MultipartFile imgFile)
    {
            Member member = memberService.memberCreate(dto, imgFile);
            CommonResDto commonResDto =
                    new CommonResDto(HttpStatus.CREATED, "회원가입 성공.", " 회원 번호 : " + member.getId() );
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    // 회원 추가 정보 입력
    @PostMapping("/member/AddInfoUpdate")
    public ResponseEntity<CommonResDto> addInfoUpdate(@RequestBody MemberAddInfoReqDto memberAddInfoReqDto) {

        Member member = memberService.addInfoUpdate(memberAddInfoReqDto);
        // AccesToken
        String jwtToken =
                jwtTokenProvider.createToken(String.valueOf(member.getId()),member.getEmail(), member.getRole().toString(),member.getName());
        // RefreshToken
        String refreshToken =
                jwtTokenProvider.createRefreshToken(String.valueOf(member.getId()),member.getEmail(), member.getRole().toString(),member.getName());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS); // 240시간

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "소셜 로그인 회원 추가정보 입력 성공", loginInfo);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 회원 정보 조회
    @GetMapping("/infoGet") // 마이페이지 회원 정보 요청
    public ResponseEntity<?> infoGet() {
            MemberInfoResDto memberInfoResDto = memberService.infoGet();
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "내 정보 조회 성공", memberInfoResDto), HttpStatus.OK);
    }

    // 회원 정보 수정 ( 사진 제외 )
    @PostMapping("/infoUpdate")
    public ResponseEntity<CommonResDto> infoUpdate(@RequestBody MemberUpdateDto dto) {

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "마이페이지 수정 성공", memberService.infoUpdate(dto).getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/imageUpdate")
    public ResponseEntity<CommonResDto> imageUpdate(@RequestPart(value = "file") MultipartFile imgFile){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "이미지 수정 성공", "수정 이미지 경로 : "+memberService.infoImageUpdate(imgFile).getProfileImage());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto){

        Member member = memberService.login(dto);
        // AccesToken
        String jwtToken =
                jwtTokenProvider.createToken(String.valueOf(member.getId()),member.getEmail(), member.getRole().toString(),member.getName());
        // RefreshToken
        String refreshToken =
                jwtTokenProvider.createRefreshToken(String.valueOf(member.getId()),member.getEmail(), member.getRole().toString(),member.getName());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS); // 240시간

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "로그인 성공", loginInfo);

        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/get-token")
    public String token(){
        System.out.println(SecurityContextHolder.getContext());
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto) {

        String rt = dto.getRefreshToken();
        Claims claims = null;

        try {
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "invalid refresh Token"), HttpStatus.BAD_REQUEST);
        }

        String id = claims.getSubject();
        String email = claims.get("email").toString();
        String role = claims.get("role").toString();
        String name = claims.get("name").toString();


        Object obj = redisTemplate.opsForValue().get(email);
        if ( obj == null || !obj.toString().equals(rt)){
            return new ResponseEntity<>(
                    new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "invalid refresh Token"), HttpStatus.BAD_REQUEST);
        }
        String newAt = jwtTokenProvider.createToken(id,email,role,name);
        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "At is renewed", info);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }

    @GetMapping("/member-name-get/{id}")
    public ResponseEntity<?> getMemberNameById(@PathVariable("id")Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "로그인한 멤버의 이름", memberService.memberNameGet(id));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping(value="/member-profile-get/{id}")
    public ResponseEntity<?> getMemberProfileById(@PathVariable("id") Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "각 튜티의 프로필 이미지", memberService.memberProfileGet(id));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
