package com.bridge.model.detailcommon;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DetailCommonHttpResponse {

    @JsonProperty("response")
    private Response response;

}