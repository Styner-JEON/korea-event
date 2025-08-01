
import { createSession } from "@/app/_libs/session";
import { ErrorResponse, RefreshAccessTokenResponse } from "../_types/responses/refresh-access-token-response";

export async function refreshAccessToken(refreshToken: string): Promise<{
  data?: string;
  error?: Error;  
}> {  
  const url = `${process.env.NEXT_PUBLIC_AUTH_BASE_URL}/auth/${process.env.NEXT_PUBLIC_AUTH_API_VERSION}/refresh`;
  let response: Response;
  try {    
    response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-refresh-token': refreshToken,
      },
      cache: 'no-store',
    }
  ); 
  } catch (error) {    
    console.error('[Network ERROR]', error);
    return { error: new Error('Network Error') };
  }

  if (!response.ok) {
    let errorJson: ErrorResponse;
    try {
      errorJson = await response.json();
    } catch (error) {
      console.error('[Error Response Parsing ERROR]', error);
      return { error: new Error('Error Response Parsing Error') };
    }

    // let detailedMessage = message;
    // const httpStatus = response.status;
    // switch (httpStatus) {
    //   case 404:
    //     detailedMessage = '요청하신 데이터를 찾을 수 없습니다.'; 
    //     break;
    //   case 401:
    //     detailedMessage = '토큰 갱신에 대해 인증 에러가 발생했습니다.'; 
    //     break;
    //   case 500:
    //     detailedMessage = '토큰 갱신에 대해 서버 에러가 발생했습니다.'; 
    //     break;
    // }
    console.error('[Backend ERROR]', response.status, errorJson.message);
    return { error: new Error(errorJson.message) };
  }

  let responseJson: RefreshAccessTokenResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { error: new Error('Response Parsing Error') };
  }
  
  const { accessToken, accessTokenExpiry, user } = responseJson;
  await createSession('username', user.name, '/', accessTokenExpiry);
  await createSession('access-token', accessToken, '/', accessTokenExpiry);  

  console.log('[액세스 토큰 갱신 완료]', responseJson);
  return { data: accessToken };
}