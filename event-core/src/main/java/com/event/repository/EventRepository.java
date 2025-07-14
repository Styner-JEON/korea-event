package com.event.repository;

import com.event.model.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    @Query("""
        SELECT e FROM EventEntity e
        WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.addr1) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.addr2) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.area) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.overview) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.sponsor1) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.sponsor2) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    Page<EventEntity> searchEvents(Pageable pageable, @Param("query") String query);
}
