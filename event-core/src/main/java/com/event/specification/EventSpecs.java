package com.event.specification;

import com.event.model.entity.EventEntity;
import com.event.model.entity.EventFavoriteEntity;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Locale;

/**
 * EventEntity에 대한 JPA Specification
 */
public class EventSpecs {

    /**
     * 검색어를 기반으로 EventEntity를 필터링하는 Specification
     *
     * @param query 검색어
     * @return 검색어에 해당하는 EventEntity를 반환하는 Specification
     */
    public static Specification<EventEntity> withQuery(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            // 검색어가 null이거나 빈 문자열인 경우, 모든 결과를 반환
            if (query == null || query.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            // 검색어를 소문자로 변환
            String normalized = query.toLowerCase(Locale.ROOT);

            // %를 양쪽에 추가하여 LIKE 검색을 수행
            String queryPattern = "%" + normalized + "%";

            // 각 필드에 대해 LIKE 검색을 수행하고, OR 조건으로 결합
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), queryPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("addr1")), queryPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("addr2")), queryPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("area")), queryPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("overview")), queryPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sponsor1")), queryPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sponsor2")), queryPattern));
        };
    }

    /**
     * 지역 필터링을 위한 Specification
     *
     * @param areaList 지역 목록
     * @return 지역 필터링을 적용한 Specification
     */
    public static Specification<EventEntity> withArea(List<String> areaList) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (areaList == null || areaList.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // 지역 목록이 비어있지 않은 경우, 해당 지역에 속하는 EventEntity를 반환
            return root.get("area").in(areaList);
        };
    }

    /**
     * 유저가 즐겨찾기한 이벤트만 필터링하는 Specification
     * @param userId
     * @return
     */
    public static Specification<EventEntity> isFavoritedBy(Long userId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }

            assert criteriaQuery != null;
            Subquery<Long> longSubquery = criteriaQuery.subquery(Long.class);
            Root<EventFavoriteEntity> eventFavoriteEntityRoot = longSubquery.from(EventFavoriteEntity.class);

            longSubquery.select(criteriaBuilder.literal(1L))
                .where(
                    criteriaBuilder.equal(eventFavoriteEntityRoot.get("contentId"), root.get("contentId")),
                    criteriaBuilder.equal(eventFavoriteEntityRoot.get("userId"), userId)
                );

            return criteriaBuilder.exists(longSubquery);
        };
    }

}
