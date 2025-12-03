'use server';

import { EventFavoriteResponse } from "@/app/_types/responses/event-favorite-response";
import { checkAccessToken } from "../check-access-token";
import { ErrorResponse } from "@/app/_types/responses/error-response";
import { revalidatePath } from "next/cache";

export async function toggleFavoriteAction(contentId: string, favoriteStatus: boolean) {     
  const beforeLoginMessage = '로그인 유지기간이 만료되어 새롭게 로그인이 필요합니다.';
  const { accessToken, errorStatus } = await checkAccessToken();
  if (errorStatus) {
    return { beforeLoginMessage };
  }

  const message = '지금은 즐겨찾기를 토글할 수 없습니다. 잠시 후 다시 시도해주세요.';
  const url = `${process.env.NEXT_PUBLIC_EVENT_BASE_URL}/events/${process.env.NEXT_PUBLIC_EVENT_API_VERSION}/${contentId}/favorite`;
  
  const method = favoriteStatus ? 'DELETE' : 'POST';

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  let response: Response;

  try {    
    response = await fetch(url, {
      method,
      headers,      
    });    
  } catch (error) {
    console.error('[Network ERROR]', error);
    return { message };
  }

  if (!response.ok) {
    let errorJson: ErrorResponse;
    try {
      errorJson = await response.json();
    } catch (error) {
      console.error('[Error Response Parsing ERROR]', error);
      return { message };
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
    return { message: detailedMessage };      
  }
  
  let responseJson: EventFavoriteResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { message };
  }

  console.log(`[즐겨찾기 ${responseJson.contentId} 토글 완료]`);

  revalidatePath(`/events/${contentId}`, 'page');
}

