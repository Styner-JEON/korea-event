package com.event.security;

import com.event.config.JwtProperties;
import com.event.exception.CustomAuthenticationException;
import com.event.exception.CustomJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretString()));
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
            log.warn("accessToken expired: {}", e.getMessage(), e);
            throw new ExpiredJwtException(
                    e.getHeader(),
                    e.getClaims(),
                    "ACCESS_TOKEN_EXPIRED",
                    e
            );
        } catch (JwtException e) {
            log.error("Invalid accessToken: {}", e.getMessage(), e);
            throw new JwtException("INVALID_ACCESS_TOKEN", e);
        }

        return jws;
    }

}
