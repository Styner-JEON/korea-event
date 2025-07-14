package com.event.model.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    LocalDateTime dbUpdatedAt
) {
}
