import { fetchEvent } from "@/app/_libs/fetchers/fetch-event";
import Image from "next/image";
import Link from 'next/link';
import { cookies } from "next/headers";
import EmbeddedMap from "./_ui/embedded-map";
import CommentSection from "./_ui/comment-section";
import { formatDateToDot } from "@/libs/utils";
import FavoriteButton from "./_ui/favorite-button";

// export const dynamic = 'force-dynamic';

export default async function DetailPage({ params }: {
  params: Promise<{ contentId: string }>
}) {
  const { contentId } = await params;
  const { eventResponse, error } = await fetchEvent(contentId);
  // console.log('[eventResponse]', eventResponse);

  const cookieStore = await cookies();
  const accessToken = cookieStore.get('access-token');
  const username = cookieStore.get('username');

  let loginStatus = false;
  if (accessToken && username) {
    loginStatus = true;
  }

  if (error) {
    return (
      <main>
        <p className="text-red-500">{error.message}</p>
      </main>
    );
  }

  if (!eventResponse) {
    return (
      <main>
        <p className="p-4">지금은 데이터가 존재하지 않습니다.</p>
      </main>
    );
  }

  const { href, label } = parseHomepage(eventResponse.homepage);

  return (
    <main className="mx-auto max-w-5xl px-6 py-8 space-y-8">
      <h1 className="text-3xl font-semibold tracking-tight">{eventResponse.title}</h1>
      <FavoriteButton
        contentId={contentId}
        loginStatus={loginStatus}
        favoriteStatus={eventResponse.favoriteStatus}
      />
      <section className="relative w-full h-96 rounded-xl overflow-hidden border border-gray-200 bg-gray-100">
        <Image
          src={eventResponse.firstImage}
          alt={eventResponse.title}
          fill
          className="object-cover"
        />
      </section>

      <section className="rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="p-6">
          <div role="list" className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
            <div role="group" aria-label="주최">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>주최</span>
              </div>
              <div className="mt-1 text-gray-500">{eventResponse.sponsor1}</div>
            </div>
            <div role="group" aria-label="전화번호">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>전화번호</span>
              </div>
              <div className="mt-1 text-gray-500">{replaceBrWithSpace(eventResponse.sponsor1Tel)}</div>
            </div>
            <div role="group" aria-label="시작일">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>시작일</span>
              </div>
              <div className="mt-1 text-gray-500">{formatDateToDot(eventResponse.eventStartDate)}</div>
            </div>
            <div role="group" aria-label="종료일">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>종료일</span>
              </div>
              <div className="mt-1 text-gray-500">{formatDateToDot(eventResponse.eventEndDate)}</div>
            </div>
            <div role="group" aria-label="시간">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>시간</span>
              </div>
              <div className="mt-1 text-gray-500">{replaceBrWithSpace(eventResponse.playTime)}</div>
            </div>
            <div role="group" aria-label="이용요금">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>이용요금</span>
              </div>
              <div className="mt-1 text-gray-500">{replaceBrWithSpace(eventResponse.useTimeFestival)}</div>
            </div>
            <div role="group" aria-label="주소" className="col-span-2">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>주소</span>
              </div>
              <div className="mt-1 text-gray-500">{eventResponse.addr1}</div>
            </div>
            <div role="group" aria-label="웹사이트" className="col-span-2">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>웹사이트</span>
              </div>
              <div className="mt-1">
                <Link
                  href={href}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sky-600 hover:underline"
                >
                  {label}
                </Link>
              </div>
            </div>
            <div role="group" aria-label="상세정보" className="col-span-2">
              <div className="font-semibold text-gray-800 flex items-center gap-2">
                <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-800"></span>
                <span>상세정보</span>
              </div>
              <div className="mt-1 text-gray-400">
                {parseOverview(eventResponse.overview)}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="rounded-xl border border-gray-200 overflow-hidden">
        <EmbeddedMap
          title={eventResponse.title}
          mapX={eventResponse.mapX}
          mapY={eventResponse.mapY}
        />
      </section>
      <CommentSection
        contentId={contentId}
        loginStatus={loginStatus}
        username={username?.value}
      />
    </main>
  );
}

// Overview에 있는 <br>를 \n로 변경
function parseOverview(overview?: string): string {
  return overview?.replace(/<br\s*\/?>/gi, '\n') ?? '';
}

// Homepage에 있는 <a>를 <Link>로 변경
function parseHomepage(homepage?: string): { href: string; label: string } {
  const raw = homepage || '';
  const hrefMatch = raw.match(/href="([^"]+)"/);
  const textMatch = raw.match(/>([^<]+)</);
  const href = hrefMatch ? hrefMatch[1] : raw;
  const label = textMatch ? textMatch[1] : href;
  return { href, label };
}

// 일반 텍스트의 <br>를 공백 1칸으로 변경
function replaceBrWithSpace(text?: string): string {
  return text?.replace(/<br\s*\/?>/gi, " ") ?? '';
}