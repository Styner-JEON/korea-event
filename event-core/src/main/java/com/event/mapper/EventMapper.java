package com.event.mapper;

import com.event.model.dto.EventDto;
import com.event.model.entity.EventEntity;
import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventEntity toEventEntity(EventDto dto);

    EventListResponse toEventListResponse(EventEntity eventEntity);

    EventResponse toEventResponse(EventEntity eventEntity, boolean favoriteStatus);

}
