package com.event.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Setter
@Getter
public class EventDto {

    // 지역기반관광정보조회
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

    // 공통정보조회
    private String homepage;

    private String overview;

    // 소개정보조회
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    private String playTime;

    private String useTimeFestival;

    private String sponsor1;
    private String sponsor1Tel;

    private String sponsor2;
    private String sponsor2Tel;

    // content-service에서만 추가
    private Instant dbUpsertedAt;

}
