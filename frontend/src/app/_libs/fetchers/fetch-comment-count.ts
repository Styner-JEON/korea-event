import { ErrorResponse } from "../../_types/responses/error-response";

export async function fetchCommentCount(contentId: string) {
  const message = '지금은 댓글 개수를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.';
  const url = `${process.env.NEXT_PUBLIC_EVENT_BASE_URL}/events/${process.env.NEXT_PUBLIC_EVENT_API_VERSION}/${contentId}/comments/count`;
  
  let response: Response;  
  try {        
    response = await fetch(url, {
      next: {
        tags: [`event:${contentId}:commentCount`],
      },
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
        detailedMessage = '권한이 필요합니다.'; 
        break;
      case 500:
        detailedMessage = '서버 에러가 발생했습니다. 잠시 후 다시 시도해 주세요.'; 
        break;
    }
    console.error('[Backend ERROR]', httpStatus, errorJson.message);
    return { error: new Error(detailedMessage) };
  }

  let responseJson: number;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { error: new Error(message) };
  }

  return { commentCount: responseJson };
}
