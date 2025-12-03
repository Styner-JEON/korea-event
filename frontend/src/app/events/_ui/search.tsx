'use client';

import {
  useSearchParams,
  usePathname,
  useRouter,
} from 'next/navigation';
import { useDebouncedCallback } from 'use-debounce';

function normalizeQuery(value: string): string {
  return value
    .trim()                // 앞뒤 공백 제거
    .replace(/\s+/g, ' ')  // 중복 공백 1개로 통일
    .toLowerCase();        // 대소문자 통일
}

export default function Search({ placeholder }: {
  placeholder: string
}) {
  const searchParams = useSearchParams();
  const pathname = usePathname();
  const { replace } = useRouter();

  // 디바운싱 처리
  const handleSearch = useDebouncedCallback((term: string) => {
    const currentParams = new URLSearchParams(searchParams);
    const currentUrl = `${pathname}?${currentParams.toString()}`;

    const nextParams = new URLSearchParams(searchParams);
    nextParams.set('page', '0');

    const normalizedTerm = normalizeQuery(term);
    if (normalizedTerm.length > 0) {      
      nextParams.set('page', '0');
      nextParams.set('query', normalizedTerm);
      
    } else {      
      nextParams.delete('page');
      nextParams.delete('query');      
    }

    const nextUrl = `${pathname}?${nextParams.toString()}`;
    if (nextUrl === currentUrl) {
      return;
    }

    replace(nextUrl);
  }, 100); // 0.1초 딜레이

  return (
    <div className="flex justify-center">
      <div className="relative w-full max-w-xl">
        <input
          className="w-full h-11 rounded-full border border-gray-300 bg-white px-5 pr-10 text-sm placeholder-gray-400 outline-none focus:ring-2 focus:ring-sky-400"
          placeholder={placeholder}
          onChange={(e) => {
            handleSearch(e.target.value);
          }}
          defaultValue={searchParams.get('query') ?? ''}
        />
        {/* 돋보기 아이콘 */}
        <span className="pointer-events-none absolute inset-y-0 right-3 flex items-center">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            aria-hidden="true"
            className="h-5 w-5 text-gray-400"
          >
            <path
              fill="currentColor"
              d="M10.5 3a7.5 7.5 0 1 1 0 15 7.5 7.5 0 0 1 0-15Zm0 2a5.5 5.5 0 1 0 0 11 5.5 5.5 0 0 0 0-11Zm9.78 13.22a1 1 0 0 1-1.56 1.24l-3.17-3.17a1 1 0 1 1 1.42-1.42l3.31 3.35Z"
            />
          </svg>
        </span>
      </div>
    </div>
  );
}
