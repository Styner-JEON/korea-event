package com.event.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "event_favorite_table",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_event_favorite_content_user",
            columnNames = {"content_id", "user_id"}
        )
    }
)
@Setter
@Getter
@NoArgsConstructor
public class EventFavoriteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteId;

    private Long contentId;

    private Long userId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    public EventFavoriteEntity(Long contentId, Long userId) {
        this.contentId = contentId;
        this.userId = userId;
        this.createdAt = Instant.now();
    }

}
