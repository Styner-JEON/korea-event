import { createSession } from "@/app/_libs/session";
import { RefreshAccessTokenResponse } from "@/app/_types/responses/refresh-access-token-response";
import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";
import { ErrorResponse } from "@/app/_types/responses/error-response";

export async function GET(request: NextRequest) {
  const cookieStore = await cookies();
  const refreshToken = cookieStore.get('refresh-token')?.value;   
  const username = cookieStore.get('username')?.value;
  if (!refreshToken) {    
    return redirectToHome(request);
  }

  // const message = '지금은 서버 점검 중입니다. 잠시 후 다시 시도해주세요.';
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
    });    
  } catch (error) {
    console.error('[Network ERROR]', error);    
    await deleteCookies(cookieStore);
    return redirectToHome(request);
  }

  if (!response.ok) {
    let errorJson: ErrorResponse;
    try {
      errorJson = await response.json();
    } catch (error) {
      console.error('[Error Response Parsing ERROR]', error);    
      await deleteCookies(cookieStore);
      return redirectToHome(request);
    }

    const httpStatus = response.status;
    console.error('[Backend ERROR]', httpStatus, errorJson.message);    
    await deleteCookies(cookieStore);
    return redirectToHome(request);
  }

  let responseJson: RefreshAccessTokenResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);    
    await deleteCookies(cookieStore);
    return redirectToHome(request);
  }

  const { accessToken, accessTokenExpiry, user } = responseJson;
  await createSession('username', user.name, '/', accessTokenExpiry);
  await createSession('access-token', accessToken, '/', accessTokenExpiry);  

  console.log(`[${username} 토큰 갱신 완료]`);  
  return redirectToHome(request);
}

function redirectToHome(request: NextRequest) {
  const nextUrl = request.nextUrl;
  const baseUrl = `${nextUrl.protocol}//${nextUrl.host}`;
  const redirectPath = nextUrl.searchParams.get("redirect") || "/";
  const redirectUrl = `${baseUrl}${redirectPath}`;
  return NextResponse.redirect(redirectUrl);
}

async function deleteCookies(cookieStore: Awaited<ReturnType<typeof cookies>>) {    
  cookieStore.delete('access-token');
  cookieStore.delete('username');
  cookieStore.delete('refresh-token');
}