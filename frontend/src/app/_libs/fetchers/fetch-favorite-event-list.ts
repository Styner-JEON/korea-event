import { EventListResponse } from "../../_types/responses/event-list-response";
import { ErrorResponse } from "../../_types/responses/error-response";
import { checkAccessToken } from "../check-access-token";

export async function fetchFavoriteEventList(pageNumber: number): Promise<{
  eventListResponse?: EventListResponse;
  error?: Error;
}> {
  const message = '지금은 즐겨찾기 목록을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.';

  const baseUrl = `${process.env.NEXT_PUBLIC_EVENT_BASE_URL}/events/${process.env.NEXT_PUBLIC_EVENT_API_VERSION}/favorites`;
  const params = new URLSearchParams();
  if (pageNumber) {
    params.append("page", String(pageNumber));
  }
  // if (query) {
  //   params.append("query", query);
  // }
  // if (area) {
  //   params.append("area", area);
  // }
  const url = `${baseUrl}?${params.toString()}`;

  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };
  const { accessToken } = await checkAccessToken();
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

  let response: Response;
  
  try {
    response = await fetch(url, {
      method: 'GET',
      headers,
      cache: 'no-store',
    });    
  } catch (error) {
    console.error('[Network ERROR]', error);
    return { error: new Error(message) };
  }

  if (!response.ok) {
    let errorJson: ErrorResponse;
    try {
      errorJson = await response.json();
    } catch (error) {
      console.error('[Error Response Parsing ERROR]', error);
      return { error: new Error(message) };
    }

    let detailedMessage = message;
    const httpStatus = response.status;
    switch (httpStatus) {
      case 404:
        detailedMessage = '요청하신 데이터를 찾을 수 없습니다.'; 
        break;
      case 401:
        detailedMessage = '로그인이 필요합니다.'; 
        break;
      case 500:
        detailedMessage = '서버 에러가 발생했습니다. 잠시 후 다시 시도해 주세요.'; 
        break;
    }
    console.error('[Backend ERROR]', httpStatus, errorJson.message);
    return { error: new Error(detailedMessage) };
  }

  let responseJson: EventListResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { error: new Error(message) };
  }

  return { eventListResponse: responseJson };
}