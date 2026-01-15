package com.event.security;

import com.event.exception.CustomAuthenticationException;
import com.event.repository.EventFavoriteRepository;
import com.event.repository.EventRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터 클래스
 * 
 * 요청마다 JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${event-select-url}")
    private String eventSelectUrl;

    @Value("${comment-url.insert}")
    private String commentInsertUrl;

    @Value("${comment-url.update}")
    private String commentUpdateUrl;

    @Value("${comment-url.delete}")
    private String commentDeleteUrl;

    @Value("${event-favorite-url}")
    private String eventFavoritesUrl;

    @Value("${event-list-favorite-url}")
    private String eventListFavoritesUrl;

    private final JwtUtil jwtUtil;

    private final AuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * 필터 적용 여부를 결정하는 메서드
     * 
     * @param request HTTP 요청 객체
     * @return true면 필터를 건너뛰고, false면 필터를 적용
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.debug("Request URI = {}, Method = {}", request.getRequestURI(), request.getMethod());

        // 상세 이벤트 요청인지 확인
        boolean eventSelectStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.GET, eventSelectUrl)
                .matches(request);

        // 댓글 작성 요청인지 확인
        boolean commentInsertStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, commentInsertUrl)
                .matches(request);

        // 댓글 수정 요청인지 확인
        boolean commentUpdateStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.PATCH, commentUpdateUrl)
                .matches(request);

        // 댓글 삭제 요청인지 확인
        boolean commentDeleteStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.DELETE, commentDeleteUrl)
                .matches(request);

        // 즐찾 요청인지 확인
        boolean eventFavoriteInsertStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, eventFavoritesUrl)
                .matches(request);

        // 즐삭 요청인지 확인
        boolean eventFavoriteDeleteStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.DELETE, eventFavoritesUrl)
                .matches(request);

        // 즐찾리스트 요청인지 확인
        boolean eventListFavoriteStatus = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.GET, eventListFavoritesUrl)
                .matches(request);

        // 위의 사안에 해당되지 않으면 필터를 건너뜀
        return !(eventSelectStatus        ||
                commentInsertStatus       ||
                commentUpdateStatus       ||
                commentDeleteStatus       ||
                eventFavoriteInsertStatus ||
                eventFavoriteDeleteStatus ||
                eventListFavoriteStatus);
    }

    /**
     * JWT 인증을 수행하는 메인 필터 메서드
     * 
     * 1. 요청에서 JWT 토큰을 추출
     * 2. 토큰을 검증하고 유저 정보를 추출
     * 3. SecurityContext에 인증 정보를 설정
     * 4. 예외 발생 시 적절한 에러 처리
     * 
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException      IO 예외
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String accessToken = extractToken(request);
            if (accessToken == null || accessToken.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // JWT 토큰 검증 및 클레임 추출
            Jws<Claims> jwsClaims = jwtUtil.readJwt(accessToken);
            Claims claims = jwsClaims.getPayload();

            // 토큰에서 사용자 정보 추출
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String userRole = claims.get("userRole", String.class);

            // Spring Security 인증 객체 생성
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new CustomPrincipal(userId, username),
                    null,
                    List.of(new SimpleGrantedAuthority(userRole)));

            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 다음 필터로 요청 전달
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

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출하는 메서드
     * 
     * Authorization 헤더에서 "Bearer " 접두사를 제거하고 토큰만 반환합니다.
     * 
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 (토큰이 없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        // "Bearer "로 시작하는 Authorization 헤더에서 토큰 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

}
