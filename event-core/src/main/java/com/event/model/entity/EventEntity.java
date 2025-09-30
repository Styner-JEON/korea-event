package com.event.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_table")
@Setter
@Getter
public class EventEntity {

    @Id
    private Long contentId;

    private String title;

    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    private String addr1;
    private String addr2;

    private String area;

    private String firstImage;
    private String firstImage2;

    private Double mapX;
    private Double mapY;

    private String zipCode;

    @Column(columnDefinition = "TEXT")
    private String homepage;

    @Column(columnDefinition = "TEXT")
    private String overview;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    private String playTime;

    @Column(columnDefinition = "TEXT")
    private String useTimeFestival;

    private String sponsor1;
    private String sponsor1Tel;

    private String sponsor2;
    private String sponsor2Tel;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant dbUpsertedAt;

}
