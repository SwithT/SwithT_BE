package com.tweety.SwithT.common.Auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
	@Value("${jwt.secretKey}")
	private String secretKey;
	@Value("${jwt.expiration}")
	private int expiration;
	@Value("${jwt.secretKeyRt}")
	private String secretKeyRt;
	@Value("${jwt.expirationRt}")
	private int expirationRt;

	public String createToken(String id, String email, String role,String name) {

		Claims claims = Jwts.claims().setSubject(id);
		claims.put("email", email);  // 사용자 ID 추가
		claims.put("role", role);
		claims.put("name", name);

		Date now = new Date();
		String token = Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + (expiration * 60 * 1000L)))  // 만료시간 설정
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
		return token;
	}

	public String createRefreshToken(String id, String email, String role,String name) {

		Claims claims = Jwts.claims().setSubject(id);
		claims.put("email", email);  // 사용자 ID 추가
		claims.put("role", role);
		claims.put("name", name);

		Date now = new Date();
		String token = Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
				.signWith(SignatureAlgorithm.HS256, secretKeyRt)
				.compact();
		return token;
	}

	public Authentication getAuthentication(String token) {

		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
		UserDetails userDetails = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

	}

	public boolean validateToken(String token) {

		try {
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
