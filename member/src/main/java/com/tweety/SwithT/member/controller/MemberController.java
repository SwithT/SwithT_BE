package com.tweety.SwithT.member.controller;

import com.tweety.SwithT.common.Auth.JwtTokenProvider;
import com.tweety.SwithT.common.dto.CommonErrorDto;
import com.tweety.SwithT.common.dto.CommonResDto;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.dto.MemberLoginDto;
import com.tweety.SwithT.member.dto.MemberRefreshDto;
import com.tweety.SwithT.member.dto.MemberSaveReqDto;
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
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberSaveReqDto dto) {
        try {
            Member member = memberService.memberCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED,
                    "회원가입 성공.", " 회원 번호 : " + member.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);

        } catch (Exception e) {
            CommonResDto errorResponse = new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,
                    "회원가입 실패.", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Login is successful", loginInfo);

        // 로그 출력
        System.out.println("ID: " + member.getId());
        System.out.println("Email: " + member.getEmail());
        System.out.println("name: " + member.getName());
        System.out.println("Role: " + member.getRole().toString());
        System.out.println("Access Token: " + jwtToken);
        System.out.println("Refresh Token: " + refreshToken);

        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
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
        String newAt = jwtTokenProvider.createToken(id,email, role,name);
        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "At is renewed", info);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }



}
