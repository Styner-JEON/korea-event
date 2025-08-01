package com.auth.security;

import com.auth.model.entity.UserEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 사용자 정의 UserDetails 구현체
 *
 * Spring Security에서 사용자 인증 정보를 제공하는 클래스입니다.
 * UserEntity를 래핑하여 Spring Security가 요구하는 인터페이스를 구현합니다.
 */
@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity;

    /**
     * 사용자명 반환
     * 
     * @return 사용자명
     */
    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    /**
     * 비밀번호 반환
     * 
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    /**
     * 사용자 권한 목록 반환
     * 
     * @return 사용자 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userEntity.getUserRole().name()));
    }

    /**
     * 계정 만료 여부
     * 
     * @return true (계정이 만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부
     * 
     * @return true (계정이 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명 만료 여부
     * 
     * @return true (자격 증명이 만료되지 않음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     * 
     * @return 사용자 엔티티의 활성화 상태
     */
    @Override
    public boolean isEnabled() {
        return userEntity.isEnabled();
    }

}
