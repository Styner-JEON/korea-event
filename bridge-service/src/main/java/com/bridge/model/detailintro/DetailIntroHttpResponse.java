package com.bridge.model.detailintro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DetailIntroHttpResponse {

    @JsonProperty("response")
    private Response response;

}