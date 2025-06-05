package com.bridge.model.areabasedlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Items {

    @JsonProperty("item")
    private List<AreaBasedListItem> areaBasedListItemList;

}
