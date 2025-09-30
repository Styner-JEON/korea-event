package com.event.repository;

import com.event.model.entity.CommentEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("CommentRepository JPA 테스트")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Nested
    @DisplayName("findByContentId (페이징)")
    class FindByContentIdTest {
        @Test
        @DisplayName("댓글 15개가 존재할 때, 0페이지 10개 조회, hasNext는 true")
        void given15Comments_whenFindByContentIdWithPaging_thenReturns10AndHasNextTrue() {
            // Given: 동일한 contentId를 가진 댓글 15개 저장
            Long contentId = 1L;
            for (int i = 0; i < 15; i++) {
                commentRepository.save(createComment(contentId, i));
            }

            // 페이지 요청: 0페이지, 사이즈 10
            Pageable pageable = PageRequest.of(0, 10);

            // When: 페이징된 댓글 목록 조회
            Slice<CommentEntity> commentEntitySlice = commentRepository.findPageByContentId(contentId, pageable);

            // Then: 10개가 반환되며, 다음 페이지가 존재함을 의미하는 hasNext()는 true
            assertThat(commentEntitySlice).hasSize(10);
            assertThat(commentEntitySlice.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("findAllByContentId")
    class FindAllByContentIdTest {
        @Test
        @DisplayName("특정 콘텐츠의 모든 댓글을 조회한다")
        void givenCommentsExist_whenFindAllByContentId_thenReturnsAllComments() {
            // Given: contentId가 같은 댓글 2개 저장
            Long contentId = 2L;
            commentRepository.saveAll(List.of(
                    createComment(contentId, 0),
                    createComment(contentId, 1)
            ));

            // When
            List<CommentEntity> commentEntityList = commentRepository.findAllByContentId(contentId);

            // Then
            assertThat(commentEntityList).hasSize(2);
        }
    }

    @Nested
    @DisplayName("countByContentId")
    class CountByContentIdTest {
        @Test
        @DisplayName("특정 콘텐츠의 댓글 수를 카운트한다")
        void givenCommentsExist_whenCountByContentId_thenReturnsCorrectCount() {
            // Given: contentId가 같은 댓글 3개 저장
            Long contentId = 3L;
            for (int i = 0; i < 3; i++) {
                commentRepository.save(createComment(contentId, i));
            }

            // When
            int count = commentRepository.countByContentId(contentId);

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findByContentIdOrderByUpdatedAtDesc")
    class FindByContentIdOrderByUpdatedAtDescTest {
        @Test
        @DisplayName("최신순으로 댓글 10개만 조회하고 정렬이 정확해야 한다")
        void given20Comments_whenFindByContentIdOrderByUpdatedAtDesc_withLimit10_thenReturnsSortedTop10() {
            // Given: contentId가 같은 댓글 20개를 저장하고, 각 댓글의 updatedAt을 1분 단위로 다르게 설정
            Long contentId = 4L;
            Instant base = Instant.parse("2025-01-01T00:00:00Z"); // 기준 시각 고정
            for (int i = 0; i < 20; i++) {
                CommentEntity commentEntity = createComment(contentId, i);
                commentEntity.setUpdatedAt(base.minusSeconds(i * 60L));
                commentRepository.save(commentEntity);
            }

            int limit = 10;
            Pageable pageable = PageRequest.of(0, limit);

            // When: 최신순으로 limit 개수만 조회
            List<CommentEntity> commentEntityList =
                    commentRepository.findByContentIdOrderByUpdatedAtDesc(contentId, pageable);

            // Then: 총 10개 반환, 첫번째 댓글이 가장 최신임을 검증
            assertThat(commentEntityList).hasSize(limit);
            assertThat(commentEntityList.get(0).getUpdatedAt())
                    .isAfter(commentEntityList.get(limit - 1).getUpdatedAt());
        }
    }

    private CommentEntity createComment(Long contentId, int index) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setContent("테스트 댓글 " + index);
        commentEntity.setContentId(contentId);
        commentEntity.setUserId(1L);
        commentEntity.setUsername("tester");
        Instant now = Instant.now();
        commentEntity.setCreatedAt(now);
        commentEntity.setUpdatedAt(now);
        return commentEntity;
    }

}
