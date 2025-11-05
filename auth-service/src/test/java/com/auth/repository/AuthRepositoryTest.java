package com.auth.repository;

import com.auth.model.entity.UserEntity;
import com.auth.model.role.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository JPA 테스트")
class AuthRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("existsByUsername")
    class ExistsByUsernameTest {
        @Test
        @DisplayName("저장된 유저 이름이 존재하면 true를 반환한다")
        void givenUsernameExists_whenExistsByUsername_thenReturnsTrue() {
            // Given
            UserEntity userEntity = new UserEntity("testuser", "pass1234", "test@email.com", UserRole.ROLE_USER);
            userRepository.save(userEntity);

            // When
            boolean result = userRepository.existsByUsernameIgnoreCase("testuser");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 유저 이름이면 false를 반환한다")
        void givenUsernameDoesNotExist_whenExistsByUsername_thenReturnsFalse() {
            // Given

            // When
            boolean result = userRepository.existsByUsernameIgnoreCase("nonexistent");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmailTest {
        @Test
        @DisplayName("저장된 이메일이 존재하면 true를 반환한다")
        void givenEmailExists_whenExistsByEmail_thenReturnsTrue() {
            // Given
            UserEntity userEntity = new UserEntity("user", "pass1234", "test@example.com", UserRole.ROLE_USER);
            userRepository.save(userEntity);

            // When
            boolean result = userRepository.existsByEmail("test@example.com");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false를 반환한다")
        void givenEmailDoesNotExist_whenExistsByEmail_thenReturnsFalse() {
            // Given

            // When
            boolean result = userRepository.existsByEmail("unknown@example.com");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsernameTest {
        @Test
        @DisplayName("유저 이름으로 조회하면 UserEntity를 반환한다")
        void givenUsernameExists_whenFindByUsername_thenReturnsUser() {
            // Given
            UserEntity userEntity = new UserEntity("findme", "password123", "findme@example.com", UserRole.ROLE_USER);
            userRepository.save(userEntity);

            // When
            Optional<UserEntity> result = userRepository.findByEmail("findme");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("findme@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 유저 이름이면 빈 Optional을 반환한다")
        void givenUsernameDoesNotExist_whenFindByUsername_thenReturnsEmpty() {
            // Given

            // When
            Optional<UserEntity> result = userRepository.findByEmail("missing");

            // Then
            assertThat(result).isEmpty();
        }
    }

}
