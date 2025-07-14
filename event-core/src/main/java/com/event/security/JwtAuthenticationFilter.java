package com.event.security;

import com.event.exception.CustomAuthenticationException;
import com.event.exception.CustomJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${comment-url.insert}")
    private String commentInsertUrl;

    @Value("${comment-url.update}")
    private String commentUpdateUrl;

    @Value("${comment-url.delete}")
    private String commentDeleteUrl;

    private final JwtUtil jwtUtil;

    private final AuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.debug("Request URI = {}, Method = {}", request.getRequestURI(), request.getMethod());

        boolean isInsert = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, commentInsertUrl)
                .matches(request);
        boolean isUpdate = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.PUT, commentUpdateUrl)
                .matches(request);
        boolean isDelete = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.DELETE, commentDeleteUrl)
                .matches(request);

        return !(isInsert || isUpdate || isDelete);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String accessToken = extractToken(request);
            if (accessToken == null || accessToken.isBlank()) {
                throw new CustomAuthenticationException("NO_ACCESS_TOKEN");
            }

            Jws<Claims> jwsClaims = jwtUtil.readJwt(accessToken);
            Claims claims = jwsClaims.getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String userRole = claims.get("userRole", String.class);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new CustomPrincipal(userId, username),
                    null,
                    List.of(new SimpleGrantedAuthority(userRole)));
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            String message = e.getMessage();
            log.warn("AccessToken expired: {}", message, e);
            customAuthenticationEntryPoint.commence(request, response, new CustomAuthenticationException(message));
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            String message = e.getMessage();
            log.error("Invalid accessToken: {}", message, e);
            customAuthenticationEntryPoint.commence(request, response, new CustomAuthenticationException(message));
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            String message = e.getMessage();
            if (message.equals("NO_ACCESS_TOKEN")) {
                log.warn("No accessToken: ", e);
            }
            log.error("Authentication failed: {}", e.getMessage(), e);
            customAuthenticationEntryPoint.commence(request, response, e);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            String message = e.getMessage();
            log.error("Unexpected error during the authentication filter: {}", message, e);
            customAuthenticationEntryPoint.commence(request, response, new CustomAuthenticationException(message));
        }
    }

    private String extractToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

}
