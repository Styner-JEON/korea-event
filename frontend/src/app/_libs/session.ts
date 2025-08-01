import 'server-only';

import { cookies } from 'next/headers';
 
export async function createSession(key: string, value: string, path: string, expires?: number) {  
  const cookieStore = await cookies();

  // 현재 시각에 밀리초를 더한 값을 Date 타입으로 변환함
  let expiryDate: Date | undefined = undefined;
  if (typeof expires === 'number') {  
    expiryDate = new Date(Date.now() + expires);
  }

  cookieStore.set(key, value, {
    httpOnly: true,
    secure: false,
    sameSite: 'lax',    
    expires: expiryDate,
    path: path,
  });
}