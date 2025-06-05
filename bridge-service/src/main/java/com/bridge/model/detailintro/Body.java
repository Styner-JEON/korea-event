package com.bridge.model.detailintro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Body {

    private Integer numOfRows;

    private Long pageNo;

    private Long totalCount;

    @JsonProperty("items")
    private Items items;

}
