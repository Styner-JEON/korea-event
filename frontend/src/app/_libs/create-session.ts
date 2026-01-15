import 'server-only';

import { cookies } from 'next/headers';
 
export async function createSession(key: string, value: string, path: string, expires?: number) {  
  const cookieStore = await cookies();

  // 현재 시각에 밀리초를 더한 값을 Date 타입으로 변환함
  let expiryDate: Date | undefined = undefined;
  if (typeof expires === 'number') {  
    expiryDate = new Date(Date.now() + expires);
  }

  const isProd = process.env.NODE_ENV === 'production';
  const domain = process.env.NEXT_PUBLIC_COOKIE_DOMAIN;
  
  if (isProd) {    
    cookieStore.set(key, value, {
      expires: expiryDate,  
      domain: domain,  
      path: path,
      secure: true,
      httpOnly: true,
      sameSite: 'lax',      
    });
  } else {    
    cookieStore.set(key, value, {
      expires: expiryDate,        
      path: path,
      secure: false,
      httpOnly: true,
      sameSite: 'lax',    
    });
  }
}