'use client';

import { fetchCommentListInRcc } from "../../../_libs/fetchers/fetch-comment-list";
import CommentArticle from "./comment-article";
import useSWRInfinite from "swr/infinite";
import { useEffect, useRef } from "react";
import { createCommentListGetKey } from "../../../_libs/util";
import CommentInsertForm from "./comment-insert-form";
import { CommentScrollResponse } from "@/app/_types/responses/comment-scroll-response";
import { CommentResponse } from "@/app/_types/responses/comment-response";

export default function CommentListSection({ contentId, isLoggedIn, username }: {
  contentId: string;
  isLoggedIn: boolean;
  username?: string;
}) {
  const getKey = createCommentListGetKey(contentId);

  const { data, error, isValidating, size, setSize, mutate } = useSWRInfinite<CommentScrollResponse>(
    getKey,
    fetchCommentListInRcc, 
    {      
      initialSize: 0,
      revalidateAll: false,
      revalidateFirstPage: false,
      revalidateOnFocus: false,
      revalidateOnMount: false,
      persistSize: true,
      revalidateIfStale: true,
      dedupingInterval: 2000,
    }
  );

  // console.log('data', data);

  const pages = data ?? [];

  let hasMoreComments = true;
  if (pages.length > 0) {
    const lastPage = pages[pages.length - 1];
    if (lastPage && lastPage.nextCursor === null) {
      hasMoreComments = false;
    }
  }

  const validatingRef = useRef(isValidating);
  useEffect(() => {     
    validatingRef.current = isValidating;
  }, [isValidating]);

  // 무한 스크롤
  useEffect(() => {    

    if (!hasMoreComments) {      
      return;
    }

    const targetElement = document.getElementById("targetElement");
    if (!targetElement) {      
      return;
    }
    
    const observer = new IntersectionObserver((entries) => {    
      entries.forEach((entry) => {         
        if (entry.isIntersecting && !validatingRef.current && hasMoreComments) {                  
          observer.unobserve(entry.target);
          setSize((size) => size + 1);
        }
      });
    },{
      root: null,
      rootMargin: "300px 0px",
      threshold: 0,
    });
    observer.observe(targetElement);

    return () => {
      observer.disconnect();      
    };
  }, [hasMoreComments, size, setSize]);

  if (error) {
    return <div className="text-red-500 p-4">지금은 댓글을 불러올 수 없습니다.</div>;
  }

  return (    
    <section>
      <CommentInsertForm
        contentId={contentId}
        isLoggedIn={isLoggedIn}
        commentMutate={mutate}
      />
      <article>
        {pages.map((CommentScrollResponse, index) => (
          <div key={`page-${index}`}>
            {CommentScrollResponse?.commentResponseList?.map((commentResponse: CommentResponse) => (
              <CommentArticle
                key={commentResponse.commentId}
                comment={commentResponse}
                contentId={contentId}
                isOwner={username === commentResponse.username}
                commentMutate={mutate}
              />
            ))}          
          </div>
        ))}

        {isValidating && hasMoreComments && (
          <div className="p-3 text-center text-xs text-gray-500">LOADING…</div>
        )}
   
        <div id="targetElement" className="h-2" />
      </article>
    </section>
  );
}
