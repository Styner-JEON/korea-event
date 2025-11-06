'use server';

import { cookies } from "next/headers"

export async function logout() {
  const cookieStore = await cookies();
  const username = cookieStore.get('username')?.value;
  
  const domain = process.env.NEXT_PUBLIC_COOKIE_DOMAIN; 
  const isProd = process.env.NODE_ENV === 'production';
  if (isProd) {
    cookieStore.set('access-token', '', {        
      domain: domain,  
      path: '/',
      maxAge: 0,
    });
    cookieStore.set('refresh-token', '', {        
      domain: domain,  
      path: '/',
      maxAge: 0,
    });
    cookieStore.set('username', '', {        
      domain: domain,  
      path: '/',
      maxAge: 0,
    });
  } else {
    cookieStore.set('access-token', '', { maxAge: 0 });
    cookieStore.set('refresh-token', '', { maxAge: 0 });
    cookieStore.set('username', '', { maxAge: 0 });
  }

  console.log(`[${username} 로그아웃 완료]`);
}