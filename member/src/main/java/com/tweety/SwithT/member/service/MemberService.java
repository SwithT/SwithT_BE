package com.tweety.SwithT.member.service;

import com.tweety.SwithT.common.Service.RedisService;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.dto.MemberLoginDto;
import com.tweety.SwithT.member.dto.MemberSaveReqDto;
import com.tweety.SwithT.member.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, RedisService redisService, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
    }

    public Member login(MemberLoginDto dto){

        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(
                ()-> new EntityNotFoundException("해당 이메일은 존재하지 않습니다"));

        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public Member memberCreate(MemberSaveReqDto memberSaveReqDto) {

        memberRepository.findByEmail(memberSaveReqDto.getEmail()).ifPresent(existingMember -> {
            throw new EntityExistsException("이미 존재하는 이메일입니다.");
        });

        String encodedPassword = passwordEncoder.encode(memberSaveReqDto.getPassword());
        return memberRepository.save(memberSaveReqDto.toEntity(encodedPassword));

    }
}
