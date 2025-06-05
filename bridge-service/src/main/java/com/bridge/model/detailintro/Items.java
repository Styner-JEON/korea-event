package com.bridge.model.detailintro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Items {

    @JsonProperty("item")
    private List<DetailIntroItem> detailIntroItemList;

}
