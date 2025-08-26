package com.bridge.service;

import com.bridge.exception.CustomBatchException;
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

/**
 * 한국관광공사 공공데이터 API 클라이언트 서비스
 *
 * 이 클래스는 한국관광공사에서 제공하는 공공데이터 API들을 활용하여 전국의 이벤트 정보를 조회하는 서비스입니다.
 *
 * 지역기반관광정보조회 API: 전국 각 지역의 이벤트 목록 조회
 * 공통정보조회 API: 각 이벤트의 공통 상세 정보 조회
 * 소개정보조회 API: 각 이벤트의 소개 및 부가 정보 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDataApiClient {

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    /**
     * 공공데이터 API 서비스의 기본 경로입니다.
     */
    @Value("${public-data-api.base-url}")
    private String baseUrl;

    /**
     * 지역기반관광정보조회 API의 상세 경로입니다.
     */
    @Value("${public-data-api.endpoints.area-based-list}")
    private String areaBasedListEndpoint;

    /**
     * 공통정보조회 API 상세 경로입니다.
     */
    @Value("${public-data-api.endpoints.detail-common}")
    private String detailCommonEndpoint;

    /**
     * 소개정보조회 API 상세 경로입니다.
     */
    @Value("${public-data-api.endpoints.detail-intro}")
    private String detailIntroEndpoint;

    /**
     * API 인증을 위한 공공데이터 API 서비스 키입니다.
     */
    @Value("${public-data-api.service-key}")
    private String serviceKey;

    /**
     * 한 번의 API 호출로 가져올 수 있는 최대 데이터 건수입니다.
     */
    @Value("${public-data-api.num-of-rows}")
    private Integer numOfRows;

    /**
     * 현재 날짜에서 이 일수만큼 뺀 날짜 이후에 수정된 데이터만 조회합니다.
     * ex) 값이 7이면 지난 7일 동안 수정된 이벤트만 조회합니다.
     */
    @Value("${public-data-api.minus-days}")
    private long minusDays;

    /**
     * 지역기반관광정보조회 API를 통해 전체 이벤트 목록을 조회합니다.
     * 첫 번째 페이지부터 시작하여 더 이상 데이터가 없을 때까지 순차적으로 조회합니다.
     *
     * 동작 과정:
     * 페이지 번호 1부터 시작
     * setAreaBasedList() 메서드를 호출하여 해당 페이지 데이터 조회
     * 반환값이 true이면 다음 페이지 조회, false이면 while문 종료
     * 모든 페이지의 데이터를 하나의 리스트에 누적함
     *
     * @return 전체 이벤트 DTO 리스트 (모든 페이지의 데이터가 포함된 완전한 리스트)
     */
    public List<EventDto> getEventDtoList() {
        int pageNo = 1;
        List<EventDto> eventDtoList = new ArrayList<>();
        while (setAreaBasedList(pageNo, eventDtoList)) {
            pageNo++;
        }
        return eventDtoList;
    }

    /**
     * 순차적으로 특정 페이지의 이벤트 목록을 조회하고 EventDto 리스트에 추가합니다.
     * 
     * @param pageNo       조회할 페이지 번호 (1부터 시작)
     * @param eventDtoList 조회한 이벤트 데이터를 저장할 리스트 (참조로 전달되어 직접 수정됨)
     * @return 다음 페이지 존재 여부 (true: 다음 페이지 있음, false: 마지막 페이지)
     * @throws CustomPublicDataApiException API 호출 실패 또는 응답 파싱 실패 시 발생
     */
    public boolean setAreaBasedList(int pageNo, List<EventDto> eventDtoList) {
        String apiName = "AreaBasedList";

        // 지역기반관광정보조회 API 호출을 위한 URI 생성
        URI uri = buildAreaBasedListUri(pageNo);

        // 지역기반관광정보조회 API 호출 및 응답 수신
        ResponseEntity<String> response = fetchApiResponse(uri, apiName);
        String result = response.getBody();

        // 응답 Content-Type이 JSON인지 확인 (정상 응답 판별)
        // 공공데이터 API 공식문서에 따르면 정상응답은 JSON 포맷, 비정상응답은 XML 포맷
        if (isJson(response)) {
            log.info("page number = {}", pageNo);
            log.debug("AreaBasedList Parsing JSON");

            // JSON 문자열을 JsonNode 객체로 파싱
            JsonNode rootJsonNode = toJsonNode(apiName, result);
            JsonNode bodyJsonNode = rootJsonNode.path("response").path("body");

            // 현재 페이지에서 실제 반환된 데이터 건수 추출
            int currentNumOfRows = bodyJsonNode.path("numOfRows").asInt();
            log.info("currentNumOfRows = {}", currentNumOfRows);

            // 전체 데이터 총 건수 추출 (모든 페이지를 합친 총 개수)
            int currentTotalCount = bodyJsonNode.path("totalCount").asInt();

            // 현재 페이지에 데이터가 없으면 더 이상 페이지가 없음을 의미
            if (currentNumOfRows == 0) {
                log.debug("No more AreaBasedList");
                return false;
            }

            // 응답을 AreaBasedListHttpResponse 타입으로 변환
            AreaBasedListHttpResponse areaBasedListHttpResponse = parseJsonNode(rootJsonNode,
                    AreaBasedListHttpResponse.class);

            List<AreaBasedListItem> areaBasedListItemList = areaBasedListHttpResponse.getResponse().getBody().getItems()
                    .getAreaBasedListItemList();

            // 각 이벤트 아이템을 EventDto로 변환하여 리스트에 추가
            areaBasedListItemList.forEach(areaBasedListItem -> {
                EventDto eventDto = new EventDto();
                setEventDto(eventDto, areaBasedListItem);
                eventDtoList.add(eventDto);
            });

            // 페이징 종료 조건 확인
            // 조건 1: 요청한 건수(numOfRows)보다 실제 반환된 건수가 적은 경우
            // 조건 2: 현재까지 조회한 총 건수가 전체 건수와 같은 경우
            if (numOfRows > currentNumOfRows || currentNumOfRows * pageNo == currentTotalCount) {
                log.debug("No more AreaBasedList");
                return false;
            }
        } else {
            // JSON이 아닌 응답 (XML 에러 응답 등)을 받은 경우
            processErrorResponse(apiName, response, result);
        }

        // 다음 페이지가 존재하는 경우 true를 리턴
        return true;
    }

    /**
     * 공통정보조회 API 호출해서 기존 EventDto 객체에 추가 정보를 설정합니다.
     * 
     * @param contentId 조회할 이벤트의 고유 식별자
     * @param eventDto  정보를 추가할 이벤트 DTO 객체 (참조로 전달되어 직접 수정됨)
     * @throws CustomPublicDataApiException API 호출 실패 또는 응답 파싱 실패 시 발생
     */
    public void setDetailCommon(String contentId, EventDto eventDto) {
        String apiName = "DetailCommon";
        log.debug("contentId = {}", contentId);

        URI uri = buildDetailCommonUri(contentId);
        ResponseEntity<String> response = fetchApiResponse(uri, apiName);
        String result = response.getBody();

        if (isJson(response)) {
            log.debug("DetailCommon Parsing JSON");
            JsonNode rootJsonNode = toJsonNode(apiName, result);
            DetailCommonHttpResponse detailCommonHttpResponse = parseJsonNode(rootJsonNode,
                    DetailCommonHttpResponse.class);
            DetailCommonItem detailCommonItem = detailCommonHttpResponse.getResponse().getBody().getItems()
                    .getDetailCommonItemList().getFirst();
            setEventDto(eventDto, detailCommonItem);
        } else {
            processErrorResponse(apiName, response, result);
        }
    }

    /**
     * 소개정보조회 API 호출해서 기존 EventDto 객체에 추가 정보를 설정합니다.
     *
     * @param contentId 조회할 이벤트의 고유 식별자
     * @param eventDto  정보를 추가할 이벤트 DTO 객체 (참조로 전달되어 직접 수정됨)
     * @throws CustomPublicDataApiException API 호출 실패 또는 응답 파싱 실패 시 발생
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
            DetailIntroHttpResponse detailIntroHttpResponse = parseJsonNode(rootJsonNode,
                    DetailIntroHttpResponse.class);
            DetailIntroItem detailIntroItem = detailIntroHttpResponse.getResponse().getBody().getItems()
                    .getDetailIntroItemList().getFirst();
            setEventDto(eventDto, detailIntroItem);
        } else {
            processErrorResponse(apiName, response, result);
        }
    }

    /**
     * 지역기반관광정보조회 API를 호출하기 위한 완전한 URI를 생성합니다.
     * 모든 필수 파라미터와 선택적 파라미터를 포함하여 구성합니다.
     * 
     * @param pageNo 조회할 페이지 번호
     * @return 완전한 API 호출 URI
     */
    private URI buildAreaBasedListUri(int pageNo) {
        String yesterday = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .minusDays(minusDays)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        URI uri = UriComponentsBuilder.fromUriString(baseUrl + areaBasedListEndpoint)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileApp", "event")
                .queryParam("MobileOS", "ETC")
                .queryParam("_type", "json")
                // 아래 요소는 '한국관광공사_TourAPI활용매뉴얼(국문)_v4.3'부터 사라짐
                // .queryParam("listYN", "Y")
                .queryParam("arrange", "Q")
                .queryParam("contentTypeId", "15")
                .queryParam("numOfRows", String.valueOf(numOfRows))
                .queryParam("pageNo", pageNo)
                .queryParam("modifiedtime", yesterday)
                .build(true)
                .toUri();
        log.info("AreaBasedList URI: {}", uri);
        return uri;
    }

    /**
     * 공통정보조회 API를 호출하기 위한 완전한 URI를 생성합니다.
     * 특정 이벤트의 상세 정보를 조회하기 위한 파라미터들을 설정합니다.
     *
     * @param contentId 조회할 이벤트의 고유 식별자
     * @return 완전한 API 호출 URI
     */
    private URI buildDetailCommonUri(String contentId) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + detailCommonEndpoint)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileApp", "event")
                .queryParam("MobileOS", "ETC")
                .queryParam("_type", "json")
                // 아래 요소들은 '한국관광공사_TourAPI활용매뉴얼(국문)_v4.3'부터 삭제됨
                // .queryParam("contentTypeId", "15")
                // .queryParam("defaultYN", "Y")
                // .queryParam("firstImageYN", "N")
                // .queryParam("areacodeYN", "N")
                // .queryParam("catcodeYN", "N")
                // .queryParam("addrinfoYN", "N")
                // .queryParam("mapinfoYN", "Y")
                // .queryParam("overviewYN", "Y")
                .queryParam("contentId", contentId)
                .build(true)
                .toUri();
        log.debug("DetailCommon URI: {}", uri);
        return uri;
    }

    /**
     * 소개정보조회 API를 호출하기 위한 완전한 URI를 생성합니다.
     * 특정 이벤트의 소개 및 부가 정보를 조회하기 위한 파라미터들을 설정합니다.
     * 
     * @param contentId 조회할 이벤트의 고유 식별자
     * @return 완전한 API 호출 URI
     */
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

    /**
     * 주어진 URI로 GET 요청을 실행하고 응답을 리턴합니다.
     *
     * @param uri     요청할 API의 완전한 URI
     * @param apiName 로깅 및 에러 메시지에 사용할 API 이름
     * @return HTTP 응답 엔티티 (응답 본문과 헤더 정보 포함)
     * @throws CustomPublicDataApiException HTTP 요청 실패 시 발생
     */
    private ResponseEntity<String> fetchApiResponse(URI uri, String apiName) {
        ResponseEntity<String> response = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, clientHttpResponse) -> {
                    HttpStatusCode httpStatusCode = clientHttpResponse.getStatusCode();
                    log.error("{} API returned error: {}", apiName, httpStatusCode);
                    throw new CustomPublicDataApiException(
                            HttpStatus.BAD_GATEWAY,
                            apiName + " API returned error: " + httpStatusCode);
                })
                .toEntity(String.class);
        log.debug("{} fetching result: {}", apiName, response.getBody());

        return response;
    }

    /**
     * HTTP 응답의 Content-Type 헤더를 확인하여 JSON 형식인지 판단합니다.
     * 공공데이터 API 가이드에 따르면 정상적인 API 응답은 JSON 형식이어야 합니다.
     * 
     * @param response 확인할 HTTP 응답 엔티티
     * @return JSON 형식 여부 (true: JSON, false: 다른 형식)
     */
    private boolean isJson(ResponseEntity<String> response) {
        return Objects
                .requireNonNull(response.getHeaders().getContentType())
                .isCompatibleWith(MediaType.APPLICATION_JSON);
    }

    /**
     * HTTP 응답의 Content-Type 헤더를 확인하여 XML 형식인지 판단합니다.
     * 
     * @param response 확인할 HTTP 응답 엔티티
     * @return XML 형식 여부 (true: XML, false: 다른 형식)
     */
    private boolean isXml(ResponseEntity<String> response) {
        return Objects.requireNonNull(response.getHeaders().getContentType()).isCompatibleWith(MediaType.TEXT_XML);
    }

    /**
     * 
     * 지역기반관광정보조회 API에서 반환된 데이터를 EventDto 객체에 설정합니다.
     * 이벤트의 기본 정보와 위치 정보를 포함합니다.
     * 
     * @param eventDto          정보를 설정할 EventDto 객체
     * @param areaBasedListItem 지역기반관광정보 API 응답 아이템
     */
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

    /**
     * 공공데이터 API에서 사용하는 숫자 지역 코드를 한글 지역명으로 변환합니다.
     * 전국 17개 시도의 코드를 지원합니다.
     *
     * @param areaCode 변환할 지역 코드 (1~8, 31~39)
     * @return 한글 지역명 (알 수 없는 코드인 경우 빈 문자열)
     */
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

    /**
     * 공통정보조회 API에서 반환된 데이터를 EventDto 객체에 추가합니다.
     * 
     * @param eventDto         정보를 설정할 EventDto 객체
     * @param detailCommonItem 공통정보조회 API 응답 아이템
     */
    private void setEventDto(EventDto eventDto, DetailCommonItem detailCommonItem) {
        eventDto.setHomepage(detailCommonItem.getHomepage());
        eventDto.setOverview(detailCommonItem.getOverview());
    }

    /**
     * 소개정보조회 API에서 반환된 데이터를 EventDto 객체에 추가합니다.
     * 
     * @param eventDto        정보를 설정할 EventDto 객체
     * @param detailIntroItem 소개정보조회 API 응답 아이템
     */
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

    /**
     * JSON 문자열을 JsonNode 객체로 변환합니다.
     * 
     * @param apiName    에러 로깅에 사용할 API 이름
     * @param jsonString 파싱할 JSON 문자열
     * @return 파싱된 JsonNode 객체
     * @throws CustomPublicDataApiException JSON 파싱 실패 시 발생
     */
    private JsonNode toJsonNode(String apiName, String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response from {}: {}", apiName, e.getMessage(), e);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Error parsing JSON response from " + apiName + ": " + e.getMessage());
        }
    }

    /**
     * JsonNode를 특정 클래스 객체로 변환합니다.
     * 
     * @param <T>      변환할 객체의 타입
     * @param jsonNode 변환할 JsonNode 객체
     * @param clazz    변환할 대상 클래스
     * @return 변환된 객체 인스턴스
     * @throws CustomPublicDataApiException 객체 변환 실패 시 발생
     */
    private <T> T parseJsonNode(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Error parsing JSON to " + clazz.getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * API 호출 결과가 JSON이 아닌 경우 에러 응답으로 간주하고 처리합니다.
     * XML 형식의 에러 응답과 예상치 못한 응답 형식을 구분하여 처리합니다.
     *
     * XML 형식: 공공데이터 API의 에러 응답 형식
     * 기타 형식: 예상치 못한 응답 형식 (네트워크 오류, 서버 오류 등)
     * 
     * @param apiName  에러 로깅에 사용할 API 이름
     * @param response HTTP 응답 엔티티
     * @param result   응답 본문 내용
     * @throws CustomPublicDataApiException 모든 경우에 BAD_GATEWAY 상태로 예외 발생
     */
    private void processErrorResponse(String apiName, ResponseEntity<String> response, String result) {
        if (isXml(response)) {
            log.error("Error response from {}: {}", apiName, result);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Error response from " + apiName + ": " + result);
        } else {
            log.error("Unexpected response format from {}: {}", apiName, result);
            throw new CustomPublicDataApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Unexpected response format of " + apiName + ": " + result);
        }
    }

}