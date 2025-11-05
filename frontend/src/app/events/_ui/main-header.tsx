import { cookies } from 'next/headers';
import Link from 'next/link';
import { logout } from '../../_libs/logout';
import { redirect } from 'next/navigation';

export default async function MainHeader() {
  const cookieStore = await cookies();
  const username = cookieStore.get('username')?.value;
  const accessToken = cookieStore.get('access-token')?.value;
  const refreshToken = cookieStore.get('refresh-token')?.value;

  async function handleLogout() {
    'use server';
    await logout();
    redirect('/');
  }

  if (refreshToken && (!username || !accessToken)) {
    // redirect(`/api/refresh?redirect=/`);     
    redirect(`/api/refresh`);
  }

  return (
    <header className="sticky top-0 z-30 bg-white/80 backdrop-blur border-b border-gray-100">
      <div className="mx-auto max-w-6xl px-6 h-16 flex items-center justify-between">
        <Link href="/" className="text-xl font-semibold tracking-tight text-gray-900">
          KoreaEvent
        </Link>
        <article>
          {username && accessToken ? (
            <form action={handleLogout} className="flex items-center gap-3">
              <p className="text-sm text-gray-700">
                <span className="hidden sm:inline">Welcome, </span>
                <strong>{username}</strong>
              </p>
              <button
                type="submit"
                className="rounded-md px-3 py-1.5 text-sm font-medium text-white bg-sky-400 hover:bg-sky-600 hover:cursor-pointer"
              >
                로그아웃
              </button>
            </form>
          ) : (
            <div className="flex items-center gap-3">
              <Link href="/login" className="rounded-md px-3 py-1.5 text-sm font-medium text-white bg-sky-400 hover:bg-sky-600">
                로그인
              </Link>
              <Link href="/signup" className="rounded-md px-3 py-1.5 text-sm font-medium text-white bg-sky-400 hover:bg-sky-600">
                회원가입
              </Link>
            </div>
          )}
        </article>
      </div>
    </header>
  );
}