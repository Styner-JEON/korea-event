import { fetchEvent } from "@/app/_libs/fetchers/fetch-event";
import Image from "next/image";
import Link from 'next/link';
import { cookies } from "next/headers";
import GoBackButton from "./_ui/go-back-button";
import EmbeddedMap from "./_ui/embedded-map";
import CommentSection from "./_ui/comment-section";
 
// export const dynamic = 'force-dynamic';

export default async function DetailPage({ params }: { 
  params: Promise<{ contentId: string }> 
}) {  
  const { contentId } = await params;
  const { eventResponse, error } = await fetchEvent(contentId);  

  const cookieStore = await cookies();
  const accessToken = cookieStore.get('access-token');
  const username = cookieStore.get('username');

  let isLoggedIn = false;
  if (accessToken && username) {
    isLoggedIn = true;
  }

  if (error) {
    return (
      <>
        <GoBackButton />
        <main>
          <p className="text-red-500">{error.message}</p>
        </main>
      </>
    );
  }

  if (!eventResponse) {
    return (
      <>
        <GoBackButton />
        <main>
          <p className="p-4">지금은 데이터가 존재하지 않습니다.</p>
        </main>
      </>
    );
  }

  const { href, label } = parseHomepage(eventResponse.homepage);

  return (
    <>
      <GoBackButton />
      <main className="p-8 space-y-6">
        <h1 className="text-2xl font-bold">{eventResponse.title}</h1>
        <p className="relative w-150 h-100">        
          <Image
            src={eventResponse.firstImage}
            alt={eventResponse.title}
            fill
            className="object-cover rounded-md"
          />        
        </p>
        <p>
          <strong>주최:</strong> {eventResponse.sponsor1}
        </p>
        <p>
          <strong>주소:</strong> {eventResponse.addr1}
        </p>
        <p>
          <strong>전화번호:</strong> {replaceBrWithSpace(eventResponse.sponsor1Tel)}          
        </p>      
        <p>
          <strong>시작일:</strong> {eventResponse.eventStartDate}
        </p>
        <p>
          <strong>종료일:</strong> {eventResponse.eventEndDate}
        </p>
        <p>
          <strong>시간:</strong> {replaceBrWithSpace(eventResponse.playTime)}
        </p>
        <p>
          <strong>이용요금:</strong> {replaceBrWithSpace(eventResponse.useTimeFestival)}          
        </p>
        <p>
          <strong>상세정보:</strong> {parseOverview(eventResponse.overview)}          
        </p>
        <p>
          <strong>웹사이트: </strong>
          <Link
            href={href}
            target="_blank"
            rel="noopener noreferrer"
            className="text-blue-600 hover:underline"
          >
            {label}
          </Link>
        </p>            
        <EmbeddedMap
          title={eventResponse.title}
          mapX={eventResponse.mapX}
          mapY={eventResponse.mapY}          
        />                
        <CommentSection
          contentId={contentId}
          isLoggedIn={isLoggedIn}
          username={username?.value}
        />        
      </main>
    </>
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