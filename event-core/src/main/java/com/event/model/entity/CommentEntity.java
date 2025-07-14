package com.event.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_table")
@Setter
@Getter
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Long contentId;

    private Long userId;
    private String username;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
