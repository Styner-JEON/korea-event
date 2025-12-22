package com.event.security;

import com.event.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JWT 토큰 처리 유틸리티 클래스
 */
@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        // Base64URL로 인코딩된 비밀키 문자열을 디코딩하여 SecretKey 객체 생성
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretString()));
    }

    /**
     * 주어진 JWT 토큰의 서명을 검증하고, 유효한 경우 클레임 정보를 반환합니다.
     * 
     * @param token 검증할 JWT 토큰 문자열
     * @return Jws<Claims> 검증된 JWT의 클레임 정보
     * @throws ExpiredJwtException 토큰이 만료된 경우
     * @throws JwtException        토큰이 유효하지 않은 경우
     */
    public Jws<Claims> readJwt(String token) {
        Jws<Claims> jws;
        try {
            // JWT 파서를 생성하고 비밀키로 서명 검증 후 클레임 파싱
            jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
            ;
        } catch (ExpiredJwtException e) {
            log.warn("accessToken expired: {}", e.getMessage(), e);
            throw new ExpiredJwtException(
                    e.getHeader(),
                    e.getClaims(),
                    "ACCESS_TOKEN_EXPIRED", // 커스텀 메시지로 변경
                    e);
        } catch (JwtException e) {
            log.error("Invalid accessToken: {}", e.getMessage(), e);
            throw new JwtException("INVALID_ACCESS_TOKEN", e); // 커스텀 메시지로 변경
        }

        return jws;
    }

}
