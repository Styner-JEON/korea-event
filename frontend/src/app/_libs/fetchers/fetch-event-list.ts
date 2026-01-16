import { EventListResponse } from "../../_types/responses/event-list-response";
import { ErrorResponse } from "../../_types/responses/error-response";

export async function fetchEventList(pageNumber: number, query: string, areaString?: string): Promise<{
  eventListResponse?: EventListResponse;
  error?: Error;
}> {
  const message = '지금은 이벤트 목록을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.';

  const baseUrl = `${process.env.NEXT_PUBLIC_EVENT_BASE_URL}/events/${process.env.NEXT_PUBLIC_EVENT_API_VERSION}`;
  const params = new URLSearchParams();
  if (pageNumber) {
    params.append("page", String(pageNumber));
  }
  if (query) {
    params.append("query", query);
  }
  if (areaString) {
    params.append("area", areaString);
  }
  const url = `${baseUrl}?${params.toString()}`;

  let response: Response;
  try {
    response = await fetch(url, {
      next: { revalidate: 60 * 60 * 12 },
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

  // const parsedResJson = EventListResponseSchema.safeParse(resJson);
  // if (!parsedResJson.success) {
  //   const parsedError = parsedResJson.error;
  //   console.error(parsedError.issues);

  //   return {      
  //     error: new Error(parsedError.message),
  //   };
  // }

  return { eventListResponse: responseJson };
}