package com.bridge.model.detailcommon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Items {

    @JsonProperty("item")
    private List<DetailCommonItem> detailCommonItemList;

}
