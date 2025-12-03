package com.event.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record EventResponse(
    Long contentId,
    String title,
    LocalDateTime createdTime,
    LocalDateTime modifiedTime,
    String addr1,
    String addr2,
    String area,
    String firstImage,
    String firstImage2,
    Double mapX,
    Double mapY,
    String zipCode,
    String homepage,
    String overview,
    LocalDate eventStartDate,
    LocalDate eventEndDate,
    String playTime,
    String useTimeFestival,
    String sponsor1,
    String sponsor1Tel,
    String sponsor2,
    String sponsor2Tel,
    Instant dbUpsertedAt,
    boolean favoriteStatus
) {
}
