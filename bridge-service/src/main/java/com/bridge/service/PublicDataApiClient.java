package com.bridge.service;

import com.bridge.exception.CustomPublicDataApiException;
import com.bridge.model.areabasedlist.AreaBasedListHttpResponse;
import com.bridge.model.areabasedlist.AreaBasedListItem;
import com.bridge.model.detailcommon.DetailCommonHttpResponse;
import com.bridge.model.detailcommon.DetailCommonItem;
import com.bridge.model.detailintro.DetailIntroHttpResponse;
import com.bridge.model.detailintro.DetailIntroItem;
import com.bridge.model.dto.EventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDataApiClient {

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    @Value("${public-data-api.base-url}")
    private String baseUrl;

    @Value("${public-data-api.endpoints.area-based-list}")
    private String areaBasedListEndpoint;

    @Value("${public-data-api.endpoints.detail-common}")
    private String detailCommonEndpoint;

    @Value("${public-data-api.endpoints.detail-intro}")
    private String detailIntroEndpoint;

    @Value("${public-data-api.service-key}")
    private String serviceKey;

    @Value("${public-data-api.num-of-rows}")
    private Integer numOfRows;

    @Value("${public-data-api.minus-days}")
    private long minusDays;

    public List<EventDto> getEventDtoList() {
        int pageNo = 1;
        List<EventDto> eventDtoList = new ArrayList<>();
        while (setAreaBasedList(pageNo, eventDtoList)) {
            pageNo++;
        }
        return eventDtoList;
    }

    /**
     * 지역기반관광정보조회 API 호출해서 세팅
      * @return
     */
    public boolean setAreaBasedList(int pageNo, List<EventDto> eventDtoList) {
        String apiName = "AreaBasedList";
        URI uri = buildAreaBasedListUri(pageNo);
        ResponseEntity<String> response = fetchApiResponse(uri, apiName);
        String result = response.getBody();
        if (isJson(response)) {
            log.info("page number = {}", pageNo);
            log.debug("AreaBasedList Parsing JSON");
            JsonNode rootJsonNode  = toJsonNode(apiName, result);
            JsonNode bodyJsonNode = rootJsonNode.path("response").path("body");
            int currentNumOfRows = bodyJsonNode.path("numOfRows").asInt();
            log.info("currentNumOfRows = {}", currentNumOfRows);
            int currentTotalCount = bodyJsonNode.path("totalCount").asInt();

            if (currentNumOfRows == 0) {
                log.debug("No more AreaBasedList");
                return false;
            }

            AreaBasedListHttpResponse areaBasedListHttpResponse = parseJsonNode(rootJsonNode, AreaBasedListHttpResponse.class);
            List<AreaBasedListItem> areaBasedListItemList = areaBasedListHttpResponse.getResponse().getBody().getItems().getAreaBasedListItemList();
            areaBasedListItemList.forEach(areaBasedListItem -> {
                EventDto eventDto = new EventDto();
                setEventDto(eventDto, areaBasedListItem);
                eventDtoList.add(eventDto);
            });

            if (numOfRows > currentNumOfRows || currentNumOfRows * pageNo == currentTotalCount) {
                log.debug("No more AreaBasedList");
                return false;
            }
        } else processErrorResponse(apiName, response, result);

        return true;
    }

    /**
     * 공통정보조회 API 호출해서 세팅
     * @param contentId
     */
    public void setDetailCommon(String contentId, EventDto eventDto) {
        String apiName = "DetailCommon";
        log.debug("contentId = {}", contentId);
        URI uri = buildDetailCommonUri(contentId);
        ResponseEntity<String> response = fetchApiResponse(uri, apiName);
        String result = response.getBody();
        if (isJson(response)) {
            log.debug("DetailCommon Parsing JSON");
            JsonNode rootJsonNode  = toJsonNode(apiName, result);
            DetailCommonHttpResponse detailCommonHttpResponse = parseJsonNode(rootJsonNode, DetailCommonHttpResponse.class);
            DetailCommonItem detailCommonItem = detailCommonHttpResponse.getResponse().getBody().getItems().getDetailCommonItemList().getFirst();
            setEventDto(eventDto, detailCommonItem);
        } else processErrorResponse(apiName, response, result);
    }

    /**
     * 소개정보조회 API 호출해서 세팅
     * @param contentId
     */
    public void setDetailIntro(String contentId, EventDto eventDto) {
        String apiName = "DetailIntro";
        log.debug("contentId = {}", contentId);
        URI uri = buildDetailIntroUri(contentId);
        ResponseEntity<String> response = fetchApiResponse(uri, apiName);
        String result = response.getBody();
        if (isJson(response)) {
            log.debug("DetailIntro Parsing JSON");
            JsonNode rootJsonNode = toJsonNode(apiName, result);
            DetailIntroHttpResponse detailIntroHttpResponse = parseJsonNode(rootJsonNode, DetailIntroHttpResponse.class);
            DetailIntroItem detailIntroItem = detailIntroHttpResponse.getResponse().getBody().getItems().getDetailIntroItemList().getFirst();
            setEventDto(eventDto, detailIntroItem);
        } else processErrorResponse(apiName, response, result);
    }

    private URI buildAreaBasedListUri(int pageNo) {
        String yesterday = LocalDate.now(ZoneId.of("Asia/Seoul"))
                    .minusDays(minusDays)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        URI uri = UriComponentsBuilder.fromUriString(baseUrl + areaBasedListEndpoint)  // 지역기반관광정보조회
                .queryParam("serviceKey", serviceKey)                            // 공공데이터포털에서 발급받은인증키
                .queryParam("MobileApp", "event")                        // 서비스명
                .queryParam("MobileOS", "ETC")                           // OS 구분(IOS, AND, ETC)
                .queryParam("_type", "json")                             // 응답메세지 형식(XML, JSON)
                .queryParam("listYN", "Y")                               // 목록 구분(Y=목록, N=개수)
                .queryParam("arrange", "Q")                              // (A=제목순, C=수정일순, D=생성일순) 대표이미지가 존재하는 정렬(O=제목순, Q=수정일순, R=생성일순)
                .queryParam("contentTypeId", "15")                       // 콘텐츠 타입(15=행사/공연/축제)
                .queryParam("numOfRows", String.valueOf(numOfRows))              // 페이지당 데이터 수
                .queryParam("pageNo", pageNo)                                    // 현재 페이지 번호
                .queryParam("modifiedtime", yesterday)                           // 수정일
                .build(true)
                .toUri();
        log.debug("AreaBasedList URI: {}", uri);
        return uri;
    }

    private URI buildDetailCommonUri(String contentId) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + detailCommonEndpoint)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileApp", "event")
                .queryParam("MobileOS", "ETC")
                .queryParam("_type", "json")
                .queryParam("contentTypeId", "15")
                .queryParam("defaultYN", "Y")
                .queryParam("firstImageYN", "N")
                .queryParam("areacodeYN", "N")
                .queryParam("catcodeYN", "N")
                .queryParam("addrinfoYN", "N")
                .queryParam("mapinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("contentId", contentId)
                .build(true)
                .toUri();
        log.debug("DetailCommon URI: {}", uri);
        return uri;
    }

    private URI buildDetailIntroUri(String contentId) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + detailIntroEndpoint)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileApp", "event")
                .queryParam("MobileOS", "ETC")
                .queryParam("_type", "json")
                .queryParam("contentTypeId", "15")
                .queryParam("contentId", contentId)
                .build(true)
                .toUri();
        log.debug("DetailIntro URI: {}", uri);
        return uri;
    }

    private ResponseEntity<String> fetchApiResponse(URI uri, String apiName) {
        ResponseEntity<String> response = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, clientHttpResponse) -> {
                    HttpStatusCode httpStatusCode = clientHttpResponse.getStatusCode();
                    log.error("{} API returned error: {}", apiName, httpStatusCode);
                    throw new CustomPublicDataApiException(
                            HttpStatus.BAD_GATEWAY,
                            apiName + " API returned error: " + httpStatusCode
                    );
                })
                .toEntity(String.class);
        log.debug("{} fetching result: {}", apiName, response.getBody());
        return response;
    }

    private boolean isJson(ResponseEntity<String> response) {
        return Objects.requireNonNull(response.getHeaders().getContentType()).isCompatibleWith(MediaType.APPLICATION_JSON);
    }

    private boolean isXml(ResponseEntity<String> response) {
        return Objects.requireNonNull(response.getHeaders().getContentType()).isCompatibleWith(MediaType.TEXT_XML);
    }

    private void setEventDto(EventDto eventDto, AreaBasedListItem areaBasedListItem) {
        eventDto.setContentId(areaBasedListItem.getContentId());
        eventDto.setCreatedTime(areaBasedListItem.getCreatedTime());
        eventDto.setModifiedTime(areaBasedListItem.getModifiedTime());
        eventDto.setAddr1(areaBasedListItem.getAddr1());
        eventDto.setAddr2(areaBasedListItem.getAddr2());
        eventDto.setArea(toArea(areaBasedListItem.getAreaCode()));
        eventDto.setFirstImage(areaBasedListItem.getFirstImage());
        eventDto.setFirstImage2(areaBasedListItem.getFirstImage2());
        eventDto.setMapX(areaBasedListItem.getMapX());
        eventDto.setMapY(areaBasedListItem.getMapY());
        eventDto.setTitle(areaBasedListItem.getTitle());
        eventDto.setZipCode(areaBasedListItem.getZipCode());
    }

    private String toArea(Integer areaCode) {
        return switch (areaCode) {
            case 1 -> "서울";
            case 2 -> "인천";
            case 3 -> "대전";
            case 4 -> "대구";
            case 5 -> "광주";
            case 6 -> "부산";
            case 7 -> "울산";
            case 8 -> "세종";
            case 31 -> "경기";
            case 32 -> "강원";
            case 33 -> "충북";
            case 34 -> "충남";
            case 35 -> "경북";
            case 36 -> "경남";
            case 37 -> "전북";
            case 38 -> "전남";
            case 39 -> "제주";
            default -> "";
        };
    }

    private void setEventDto(EventDto eventDto, DetailCommonItem detailCommonItem) {
        eventDto.setHomepage(detailCommonItem.getHomepage());
        eventDto.setOverview(detailCommonItem.getOverview());
    }

    private void setEventDto(EventDto eventDto, DetailIntroItem detailIntroItem) {
        eventDto.setEventStartDate(detailIntroItem.getEventStartDate());
        eventDto.setEventEndDate(detailIntroItem.getEventEndDate());
        eventDto.setPlayTime(detailIntroItem.getPlayTime());
        eventDto.setUseTimeFestival(detailIntroItem.getUseTimeFestival());
        eventDto.setSponsor1(detailIntroItem.getSponsor1());
        eventDto.setSponsor1Tel(detailIntroItem.getSponsor1Tel());
        eventDto.setSponsor2(detailIntroItem.getSponsor2());
        eventDto.setSponsor2Tel(detailIntroItem.getSponsor2Tel());
    }

    private JsonNode toJsonNode(String apiName, String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response from {}: {}", apiName, e.getMessage(), e);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Error parsing JSON response from " + apiName + ": " + e.getMessage()
            );
        }
    }

    private <T> T parseJsonNode(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Error parsing JSON to " + clazz.getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    private void processErrorResponse(String apiName, ResponseEntity<String> response, String result) {
        if (isXml(response)) {
            log.error("Error response from {}: {}", apiName, result);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Error response from " + apiName + ": " + result
            );
        } else {
            log.error("Unexpected response format from {}: {}", apiName, result);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Unexpected response format of " + apiName + ": " + result
            );
        }
    }

}