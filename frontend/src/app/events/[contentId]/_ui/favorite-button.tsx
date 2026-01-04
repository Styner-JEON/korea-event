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
      return;
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
      title={pending ? '즐겨찾기 처리 중입니다.' : undefined}
      className={`
        inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5
        text-sm font-medium transition
        ${pending
          ? 'cursor-not-allowed opacity-50'
          : 'cursor-pointer hover:bg-gray-100'}
      `}
    >
      <span
        className={`text-base leading-none ${
          pending
            ? 'text-gray-400'
            : active
              ? 'text-yellow-400'
              : 'text-gray-300'
        }`}
      >
        {active ? '★' : '☆'}
      </span>
      <span>
        {pending ? '즐겨찾기 중' : '즐겨찾기'}
      </span>
    </button>
  );
}