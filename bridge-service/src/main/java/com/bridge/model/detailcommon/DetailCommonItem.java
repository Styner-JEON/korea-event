package com.bridge.model.detailcommon;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class DetailCommonItem {

    @JsonProperty("contentid")
    private Long contentId;
    @JsonProperty("contenttypeid")
    private Integer contentTypeId;

    private String title;

    @JsonProperty("createdtime")
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime createdTime;

    @JsonProperty("modifiedtime")
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime modifiedTime;

    private String tel;

    @JsonProperty("telname")
    private String telName;

    private String homepage;

    @JsonProperty("booktour")
    private String bookTour;

    @JsonProperty("mapx")
    private Double mapX;
    @JsonProperty("mapy")
    private Double mapY;

    @JsonProperty("mlevel")
    private Integer mLevel;

    private String overview;

}
