import { cookies } from 'next/headers';
import Link from 'next/link';
import { logout } from '../../_libs/logout';
import { redirect } from 'next/navigation';

export default async function MainHeader() {  
  const cookieStore = await cookies();
  const username = cookieStore.get('username')?.value;
  const accessToken = cookieStore.get('access-token')?.value;
  const refreshToken = cookieStore.get('refresh-token')?.value;

  if (refreshToken && (!username || !accessToken)) {
    // redirect(`/api/refresh?redirect=/`);     
    redirect(`/api/refresh`); 
  }

  return (
    <header className="flex justify-between items-center">
      <Link href="/">home</Link>
      <article>
        {username && accessToken ? (
          <form action={logout} className="flex items-center">
            <p className="text-lg inline-block mr-4">
              Welcome, <strong>{username}</strong>!
            </p>
            <button
              type="submit"
              className="text-red-500 hover:underline ml-2"
            >
              Logout
            </button>
          </form>
        ) : (
          <div className="flex flex-col items-end">
            <div className="flex space-x-4 items-center">
              <Link href="/login" className="text-blue-600 hover:underline">
                Login
              </Link>
              <Link href="/signup" className="text-blue-600 hover:underline">
                Signup
              </Link>
            </div>
          </div>
        )}
      </article>
    </header>
  );
}