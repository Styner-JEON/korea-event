'use server';

import { cookies } from "next/headers"

export async function logout() {
  const cookieStore = await cookies();
  const username = cookieStore.get('username')?.value;
  cookieStore.delete('access-token');
  cookieStore.delete('refresh-token');
  cookieStore.delete('username');  
  console.log(`[${username} 로그아웃 완료]`);
}