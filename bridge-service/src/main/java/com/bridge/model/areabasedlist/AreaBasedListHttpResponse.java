package com.bridge.model.areabasedlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AreaBasedListHttpResponse {

    @JsonProperty("response")
    private Response response;

}