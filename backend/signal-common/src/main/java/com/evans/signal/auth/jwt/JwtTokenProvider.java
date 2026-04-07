package com.evans.signal.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long validityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long validityInMilliseconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(Long userId, String userName) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 주제(Subject)에 userId 저장
                .claim("email", userName)              // 비공개 클레임에 email 저장
                .setIssuedAt(now)                   // 발행 시간
                .setExpiration(validity)            // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘
                .compact();
    }

    // 인증 정보 조회
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        // 클레임에서 권한 정보 가져오기 (지금은 권한이 없으니 빈 리스트 혹은 ROLE_USER)
        
        // Principal에 userId or email 저장
        Long userId = Long.parseLong(claims.getSubject());
        
        // UserDetails 대신 간단한 Principal 사용 (DB 조회 없이 토큰 정보만으로 인증 객체 생성)
        return new UsernamePasswordAuthenticationToken(userId, "", Collections.emptyList());
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 만료되었거나, 위조되었거나, 형식이 잘못된 경우 false 반환
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
