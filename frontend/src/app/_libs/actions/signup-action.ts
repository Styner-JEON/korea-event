'use server';

import { SignupFormSchema, SignupFormState } from "./definitions/signup-form-definition";
import { redirect } from "next/navigation";
import { SignupResponse } from "../../_types/responses/signup-response";
import { ErrorResponse } from "../../_types/responses/error-response";

export async function signupAction(state: SignupFormState, formData: FormData) {    
  const validatedFields = SignupFormSchema.safeParse({
    username: formData.get('username'),
    email: formData.get('email'),
    password: formData.get('password'),
  });
 
  if (!validatedFields.success) {
    return {  
      errors: validatedFields.error.flatten().fieldErrors
    };
  }
 
  const { username, email, password } = validatedFields.data;

  const message = '지금은 회원가입을 할 수 없습니다. 잠시 후 다시 시도해주세요.';
  const url = `${process.env.NEXT_PUBLIC_AUTH_BASE_URL}/auth/${process.env.NEXT_PUBLIC_AUTH_API_VERSION}/signup`;
  let response: Response;
  try {    
    response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username,
        email,        
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
      case 409:
        detailedMessage = '아이디나 이메일이 이미 존재합니다.'; 
        break;
      case 500:
        detailedMessage = '서버 에러가 발생했습니다. 잠시 후 다시 시도해 주세요.'; 
        break;
    }
    console.error('[Backend ERROR]', httpStatus, errorJson.message);
    return { message: detailedMessage };
  }

  let responseJson: SignupResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { message };
  }

  console.log(`[${responseJson.username} 회원가입 완료]`);
  redirect('/');
}
