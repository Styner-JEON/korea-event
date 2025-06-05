package com.bridge.model.areabasedlist;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class AreaBasedListItem {

    /**
     * 콘텐츠 ID
     */
    @JsonProperty("contentid")
    private Long contentId;

    /**
     * 콘텐츠
     * 15=행사/공연/축제
     */
    @JsonProperty("contenttypeid")
    private Integer contentTypeId;

    /**
     * 콘텐츠 제목
     */
    private String title;

    /**
     * 콘텐츠 생성일
     */
    @JsonProperty("createdtime")
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime createdTime;

    /**
     * 콘텐츠 수정일
     */
    @JsonProperty("modifiedtime")
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime modifiedTime;

    /**
     * 주소
     */
    private String addr1;

    /**
     * 상세 주소
     */
    private String addr2;

    /**
     * { "code": "1", "name": "서울" },
     * { "code": "2", "name": "인천" },
     * { "code": "3", "name": "대전" },
     * { "code": "4", "name": "대구" },
     * { "code": "5", "name": "광주" },
     * { "code": "6", "name": "부산" },
     * { "code": "7", "name": "울산" },
     * { "code": "8", "name": "세종특별자치시" },
     * { "code": "31", "name": "경기도" },
     * { "code": "32", "name": "강원특별자치도" },
     * { "code": "33", "name": "충청북도" },
     * { "code": "34", "name": "충청남도" },
     * { "code": "35", "name": "경상북도" },
     * { "code": "36", "name": "경상남도" },
     * { "code": "37", "name": "전북특별자치도" },
     * { "code": "38", "name": "전라남도" },
     * { "code": "39", "name": "제주도" }
     */
    @JsonProperty("areacode")
    private Integer areaCode;

    /**
     * 교과서 속의 여행인지 여부
     */
    @JsonProperty("booktour")
    private String bookTour;

    /**
     * 대분류
     */
    private String cat1;

    /**
     * 중분류
     */
    private String cat2;

    /**
     * 소분류
     */
    private String cat3;

    /**
     * 대표이미지(원본)
     */
    @JsonProperty("firstimage")
    private String firstImage;

    /**
     * 대표이미지(썸네일)
     */
    @JsonProperty("firstimage2")
    private String firstImage2;

    /**
     * 저작권 유형
     * Type1: 제1유형(출처표시-권장)
     * Type3: 제3유형(제1유형 + 변경금지)
     */
    private String cpyrhtDivCd;

    /**
     * GPS 경도 좌표
     */
    @JsonProperty("mapx")
    private Double mapX;

    /**
     * GPS 위도 좌표
     */
    @JsonProperty("mapy")
    private Double mapY;

    @JsonProperty("mlevel")
    private Integer mLevel;

    @JsonProperty("sigungucode")
    private Integer siGunGuCode;

    /**
     * 전화번호
     */
    private String tel;

    /**
     * 우편번호
     */
    @JsonProperty("zipcode")
    private String zipCode;

}
