'use client';

import {
  useSearchParams,
  usePathname,
  useRouter,
} from 'next/navigation';
import { useState, useEffect } from 'react';

const AREAS = [
  '서울',
  '경기',
  '인천',
  '강원',
  '충북',
  '충남',
  '세종',
  '대전',
  '경북',
  '경남',
  '대구',
  '울산',
  '부산',
  '전북',
  '전남',
  '광주',
  '제주'
];

export default function AreaSelector() {
  const searchParams = useSearchParams();
  const pathname = usePathname();
  const { replace } = useRouter();

  const [selectedAreas, setSelectedAreas] = useState<string[]>([]);

  // URL에서 선택된 지역들을 읽어와서 상태에 저장
  useEffect(() => {
    const areasParam = searchParams.get('area');
    if (areasParam) {
      setSelectedAreas(areasParam.split(','));
    }
  }, [searchParams]);

  const handleAreaClick = (newSelectedArea: string) => {
    let newSelectedAreas: string[];

    if (selectedAreas.includes(newSelectedArea)) {
      // 이미 선택된 지역이면 제거
      newSelectedAreas = selectedAreas.filter(area => area !== newSelectedArea);
    } else {
      // 선택되지 않은 지역이면 추가
      newSelectedAreas = [...selectedAreas, newSelectedArea];
    }

    setSelectedAreas(newSelectedAreas);

    // URL 업데이트
    const params = new URLSearchParams(searchParams);
    params.set('page', '0');

    if (newSelectedAreas.length > 0) {
      params.set('area', newSelectedAreas.join(','));
    } else {
      params.delete('area');
    }

    replace(`${pathname}?${params.toString()}`);
  };

  return (
    <div className="w-full">
      <div className="flex flex-wrap justify-center gap-2">
        {AREAS.map((area) => {
          const selected = selectedAreas.includes(area);
          return (
            <button
              key={area}
              type="button"
              aria-pressed={selected}
              onClick={() => handleAreaClick(area)}
              className={`px-3.5 py-1.5 rounded-full text-sm border transition-colors cursor-pointer ${selected
                ? 'bg-sky-500 text-white border-sky-500'
                : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                }`}
            >
              {area}
            </button>
          );
        })}
      </div>
    </div>
  );
}