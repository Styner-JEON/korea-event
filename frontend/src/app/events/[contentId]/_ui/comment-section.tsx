import { Suspense } from "react";
import CommentAnalysisArticle from "./comment-analysis-article";
import ComponentLoading from "@/app/_ui/component-loading";
import CommentListSection from "./comment-list-section";

export default async function CommentSection({ contentId, isLoggedIn, username }: {
  contentId: string;
  isLoggedIn: boolean;
  username?: string;
}) {
  return (
    <section>
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
      <Suspense fallback={<ComponentLoading />}>
        <CommentListSection          
          contentId={contentId}
          isLoggedIn={isLoggedIn}
          username={username}        
        />
      </Suspense>      
    </section>
  );
}