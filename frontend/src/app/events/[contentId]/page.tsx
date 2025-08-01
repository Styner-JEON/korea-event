import { fetchEvent } from "@/app/_libs/fetchers/fetch-event";
import { fetchCommentList } from "@/app/_libs/fetchers/fetch-comment-list";
import Image from "next/image";
import Link from 'next/link';
import { cookies } from "next/headers";
import GoBackButton from "./_ui/go-back-button";
import EmbeddedMap from "./_ui/embedded-map";
import CommentListArticle from "./_ui/comment-list-article";
import CommentInsertForm from "./_ui/comment-insert-form";
import { Suspense } from "react";
import ComponentLoading from "@/app/_ui/component-loading";
import CommentAnalysisArticle from "./_ui/comment-analysis-article";

// export const dynamic = 'force-dynamic';

export default async function DetailPage(props: { params: Promise<{ contentId: string }> }) {
  const params = await props.params;  
  const contentId = params.contentId;
  const { eventResponse, error } = await fetchEvent(contentId);  

  const cookieStore = await cookies();
  const accessToken = cookieStore.get('access-token');
  const username = cookieStore.get('username');

  let isLoggedIn = false;
  if (accessToken && username) {
    isLoggedIn = true;
  }

  const { commentListResponse } = await fetchCommentList(contentId);
  const commentSize = commentListResponse?.numberOfElements || 0;

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

  const overviewText = parseOverview(eventResponse.overview);
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
          <strong>Address:</strong> {eventResponse.addr1} {eventResponse.addr2}
        </p>
        <p>
          <strong>Area:</strong> {eventResponse.area}
        </p>
        <p>
          <strong>Start Date:</strong> {eventResponse.eventStartDate}
        </p>
        <p>
          <strong>End Date:</strong> {eventResponse.eventEndDate}
        </p>
        <p>
          <strong>Overview:</strong> {overviewText}          
        </p>
        <p>
          <strong>Homepage:</strong>
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
        <CommentInsertForm contentId={contentId} isLoggedIn={isLoggedIn} />
        {commentSize >= 10 ? (
          <Suspense fallback={
            <div className="px-4 py-6 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="animate-pulse space-y-4">
                <div className="h-6 bg-blue-200 rounded w-1/3"></div>
                <div className="h-4 bg-blue-200 rounded w-full"></div>
                <div className="h-4 bg-blue-200 rounded w-3/4"></div>
                <div className="flex gap-2">
                  <div className="h-6 bg-blue-200 rounded-full w-16"></div>
                  <div className="h-6 bg-blue-200 rounded-full w-20"></div>
                  <div className="h-6 bg-blue-200 rounded-full w-12"></div>
                </div>
              </div>
              <p className="text-blue-600 text-center mt-4">댓글 분석 중...</p>
            </div>
          }>
            <CommentAnalysisArticle contentId={contentId} />
          </Suspense>
        ) : (
          <div className="px-4 py-6 bg-gray-50 rounded-lg">
            <p className="text-gray-600 text-center">
              댓글이 10개 이상일 때 댓글 분석 결과를 제공합니다. (현재 표시된 댓글: {commentSize}개)
            </p>
          </div>
        )}
        <Suspense fallback={<ComponentLoading />}>
          <CommentListArticle contentId={contentId} username={username?.value}/>
        </Suspense>
      </main>
    </>
  );
}

// Overview에 있는 <br>들을 \n로 변경
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
