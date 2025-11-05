package com.auth.security;

import com.auth.model.entity.UserEntity;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 정의 UserDetailsService 구현체
 *
 * Spring Security에서 사용자 정보를 로드하는 서비스입니다.
 * 데이터베이스에서 사용자 정보를 조회하여 UserDetails 객체로 변환합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자명으로 사용자 정보 로드합니다.
     * AuthService.login() 내의 authenticationManager.authenticate(authentication)에 의해 실행됩니다.
     * 
     * @param email 조회할 이메일
     * @return 사용자 정보를 담은 UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
        return new CustomUserDetails(userEntity);
    }

}
