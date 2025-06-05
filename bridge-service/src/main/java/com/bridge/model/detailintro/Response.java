package com.bridge.model.detailintro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Response {

    @JsonProperty("header")
    private Header header;

    @JsonProperty("body")
    private Body body;

}
