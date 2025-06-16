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

    private final long accessTokenExpiry;

    private final long refreshTokenExpiry;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretString()));
        this.accessTokenExpiry = jwtProperties.getAccessTokenExpiry();
        this.refreshTokenExpiry = jwtProperties.getRefreshTokenExpiry();
    }

    private String createJwt(Long userId, String username, String userRole, long expirationTime) {
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

    public String createAccessToken(Long userId, String username, String userRole) {
        return createJwt(userId, username, userRole, accessTokenExpiry);
    }

    public String createRefreshToken(Long userId, String username, String userRole) {
        return createJwt(userId, username, userRole, refreshTokenExpiry);
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
            log.warn("JWT token expired: {}", e.getMessage(), e);
            throw new CustomJwtException(HttpStatus.UNAUTHORIZED, "JWT token expired");
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage(), e);
            throw new CustomJwtException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }
        return jws;
    }

}
