'use client';

import { useRouter } from 'next/navigation';
import { useTransition } from 'react';
import { toggleFavoriteAction } from '@/app/_libs/actions/toggle-favorite-action';

export default function FavoriteButton({
  contentId,
  loginStatus,
  favoriteStatus,
}: {
  contentId: string;
  loginStatus: boolean;
  favoriteStatus: boolean;
}) {
  const router = useRouter();
  const [pending, startTransition] = useTransition();

  async function handleClick() {
    if (!loginStatus) {
      router.push(`/login`);      
    }

    startTransition(async () => {
      await toggleFavoriteAction(contentId, favoriteStatus);
      router.refresh();
    });
  }

  const active = loginStatus && favoriteStatus;

  return (
    <button
      type="button"
      onClick={handleClick}      
      disabled={pending}
      aria-pressed={active}
      className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm font-medium cursor-pointer`}
    >
      <span
        className={`text-base leading-none ${active ? 'text-yellow-400' : 'text-gray-300'
          }`}
      >
        {active ? "★" : "☆"}
      </span>
      <span>{active ? "즐겨찾기" : "즐겨찾기"}</span>
    </button>
  );
}