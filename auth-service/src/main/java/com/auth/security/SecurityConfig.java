package com.auth.security;

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

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${auth-url.login}")
    private String loginUrl;

    @Value("${auth-url.signup}")
    private String signupUrl;

    @Value("${auth-url.refresh}")
    private String refreshUrl;

    private final AuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf((csrf) -> csrf.disable())
            .authorizeHttpRequests((authorize) -> authorize
                    .requestMatchers("/auth/actuator/**").permitAll()
                    .requestMatchers("/favicon.ico").permitAll()
                    .requestMatchers(HttpMethod.POST, loginUrl).permitAll()
                    .requestMatchers(HttpMethod.POST, signupUrl).permitAll()
                    .requestMatchers(HttpMethod.POST, refreshUrl).permitAll()
                    .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .cors(withDefaults())
            .exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            ;
        return http.build();
    }

}
