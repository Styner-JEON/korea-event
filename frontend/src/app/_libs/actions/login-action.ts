'use server';

import { redirect } from "next/navigation";
import { createSession } from "../session";
import { LoginFormSchema, LoginFormState } from "./definitions/login-form-definition";
import { LoginResponse } from "../../_types/responses/login-response";
import { ErrorResponse } from "../../_types/responses/error-response";

// export const dynamic = 'force-dynamic';

export async function loginAction(state: LoginFormState, formData: FormData) { 
  const validatedFields = LoginFormSchema.safeParse({
    username: formData.get('username'),    
    password: formData.get('password')
  });
 
  if (!validatedFields.success) {
    return {  
      errors: validatedFields.error.flatten().fieldErrors
    };
  }

  const { username, password } = validatedFields.data;  

  const message = '지금은 로그인을 할 수 없습니다. 잠시 후 다시 시도해주세요.';
  const url = `${process.env.NEXT_PUBLIC_AUTH_BASE_URL}/auth/${process.env.NEXT_PUBLIC_AUTH_API_VERSION}/login`;
  let response: Response;
  try {    
    response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ 
        username, 
        password,
      }),      
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
        detailedMessage = 'ID와 password를 정확히 입력해주세요.'; 
        break;
      case 500:
        detailedMessage = '서버 에러가 발생했습니다. 잠시 후 다시 시도해 주세요.'; 
        break;
    }
    console.error('[Backend ERROR]', httpStatus, errorJson.message);
    return { message: detailedMessage };
  }

  let responseJson: LoginResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { message };
  }
  
  console.log(`[${responseJson.user.name} 로그인 완료]`);

  const { accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry, user } = responseJson;
  await createSession('username', user.name, '/', accessTokenExpiry);
  await createSession('access-token', accessToken, '/', accessTokenExpiry);
  await createSession('refresh-token', refreshToken, '/', refreshTokenExpiry);

  redirect('/')
}
