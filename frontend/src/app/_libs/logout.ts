'use server';

import { cookies } from "next/headers"

export async function logout() {
  const cookieStore = await cookies();
  const username = cookieStore.get('username')?.value;
  cookieStore.set('access-token', '', { maxAge: 0 });
  cookieStore.set('refresh-token', '', { maxAge: 0 });
  cookieStore.set('username', '', { maxAge: 0 });
  console.log(`[${username} 로그아웃 완료]`);
}