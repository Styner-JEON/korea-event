package com.auth.security;

import com.auth.config.JwtProperties;
import com.auth.exception.CustomJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretString()));
    }

    public String createJwt(Long userId, String username, String userRole, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(new Date(now + expirationTime))
                .issuedAt(new Date(now))
                .claim("username", username)
                .claim("userRole", userRole)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact()
                ;
    }

    public Jws<Claims> readJwt(String token) {
        Jws<Claims> jws;
        try {
            jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
            ;
        } catch (ExpiredJwtException e) {
            log.warn("refreshToken expired: {}", e.getMessage(), e);
            throw new CustomJwtException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED");
        } catch (JwtException e) {
            log.error("Invalid refreshToken: {}", e.getMessage(), e);
            throw new CustomJwtException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }
        return jws;
    }

}
