"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

export default function EventsToggleButton({ loginStatus }: { 
  loginStatus: boolean 
}) {
  const pathname = usePathname();

  if (!loginStatus) {
    return null;
  }

  const onFavoritesPage = pathname.startsWith("/events/favorites");  
  if (onFavoritesPage) {    
    return (
      <Link
        href="/events"
        className="rounded-md px-3 py-1.5 text-sm font-medium text-sky-600 hover:bg-sky-50 hover:cursor-pointer"
      >
        전체 목록
      </Link>
    );
  } else {
    return (
      <Link
        href="/events/favorites"
        className="rounded-md px-3 py-1.5 text-sm font-medium text-sky-600 hover:bg-sky-50 hover:cursor-pointer"
      >
        즐겨찾기 목록
      </Link>
    );
  }
}