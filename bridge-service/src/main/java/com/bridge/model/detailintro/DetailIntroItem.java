package com.bridge.model.detailintro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class DetailIntroItem {

    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("contenttypeid")
    private Integer contentTypeId;

    /**
     * 관람가능연령
     */
    @JsonProperty("agelimit")
    private String ageLimit;

    /**
     * 예매처
     */
    @JsonProperty("bookingplace")
    private String bookingPlace;

    /**
     * 할인정보
     */
    @JsonProperty("discountinfofestival")
    private String discountInfoFestival;

    /**
     * 행사 홈페이지
     */
    @JsonProperty("eventhomepage")
    private String eventHomepage;

    /**
     * 행사 장소
     */
    @JsonProperty("eventplace")
    private String eventPlace;

    /**
     * 축제 등급
     */
    @JsonProperty("festivalgrade")
    private String festivalGrade;

    /**
     * 행사장 위치안내
     */
    @JsonProperty("placeinfo")
    private String placeInfo;

    /**
     * 행사 프로그램
     */
    @JsonProperty("program")
    private String program;

    /**
     * 관람 소요시간
     */
    @JsonProperty("spendtimefestival")
    private String spendTimeFestival;

    /**
     * 부대행사
     */
    @JsonProperty("subevent")
    private String subEvent;




    /**
     * 행사시작일
     */
    @JsonProperty("eventstartdate")
    private LocalDate eventStartDate;

    /**
     * 행사종료일
     */
    @JsonProperty("eventenddate")
    private LocalDate eventEndDate;

    /**
     * 공연시간
     */
    @JsonProperty("playtime")
    private String playTime;

    /**
     * 이용요금
     */
    @JsonProperty("usetimefestival")
    private String useTimeFestival;

    /**
     * 주최자
     */
    private String sponsor1;

    @JsonProperty("sponsor1tel")
    private String sponsor1Tel;

    /**
     * 주관사
     */
    private String sponsor2;

    @JsonProperty("sponsor2tel")
    private String sponsor2Tel;

}
