'use client';

import React, { useState } from "react";
import CommentUpdateForm from "./comment-update-form";
import CommentDeleteForm from "./comment-delete-form";
import { CommentResponse } from "../../../_types/responses/comment-response";
import { SWRInfiniteResponse } from "swr/infinite";
import { CommentScrollResponse } from "@/app/_types/responses/comment-scroll-response";
import { formatDateToHyphen } from "@/libs/utils";

export default function CommentArticle({ comment, contentId, isOwner, commentMutate }: {
  comment: CommentResponse;
  contentId: string;
  isOwner: boolean;
  commentMutate: SWRInfiniteResponse<CommentScrollResponse>["mutate"];
}) {
  const [isUpdating, setIsUpdating] = useState(false);

  return (
    <div className="rounded-xl border border-gray-200 bg-white shadow-sm p-4 text-sm text-gray-800">
      {isUpdating ? (
        // 댓글 수정 모드인 경우
        <CommentUpdateForm
          comment={comment}
          contentId={contentId}
          onCancel={() => setIsUpdating(false)}
          commentMutate={commentMutate}
        />
      ) : (
        // 댓글 수정 모드가 아닌 경우
        <>
          <div className="font-semibold">{comment.username}</div>
          {/* 줄바꿈(\n)을 <br />로 변환 */}
          <div className="mt-1 leading-relaxed">
            {comment.content.split('\n').map((line, i) => (
              <React.Fragment key={i}>
                {line}
                <br />
              </React.Fragment>
            ))}
          </div>
          <div className="mt-4 text-xs text-gray-500">
            {formatDateToHyphen(comment.updatedAt)}
          </div>

          {isOwner && (
            // 댓글 작성작인 경우, 댓글 삭제 버튼과 수정 버튼          
            <div className="flex gap-2 mt-2">
              <CommentDeleteForm
                contentId={contentId}
                commentId={comment.commentId}
                commentMutate={commentMutate}
              />
              <button
                type="button"
                className="rounded-md border p-2 bg-gray-200 hover:bg-gray-300 hover:cursor-pointer"
                onClick={() => setIsUpdating(true)}
              >
                수정
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}