package com.event.model.response;

import java.time.LocalDate;

public record EventListResponse(
    Long contentId,
    String title,
    String area,
    String firstImage,
    LocalDate eventStartDate,
    LocalDate eventEndDate
) {
}
