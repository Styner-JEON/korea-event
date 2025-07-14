package com.event.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${api.version}")
    private String apiVersion;

    @Value("${comment-url.insert}")
    private String commentInsertUrl;

    @Value("${comment-url.update}")
    private String commentUpdateUrl;

    @Value("${comment-url.delete}")
    private String commentDeleteUrl;

    private final OncePerRequestFilter jwtAuthenticationFilter;

    private final AuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf((csrf) -> csrf.disable())
            .authorizeHttpRequests((authorize) -> authorize
                    .requestMatchers(HttpMethod.POST, commentInsertUrl).authenticated()
                    .requestMatchers(HttpMethod.PUT, commentUpdateUrl).authenticated()
                    .requestMatchers(HttpMethod.DELETE, commentDeleteUrl).authenticated()
                    .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .cors(withDefaults())
//            .addFilterBefore(jwtAuthenticationFilter, SecurityContextHolderFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            ;
        return http.build();
    }

}