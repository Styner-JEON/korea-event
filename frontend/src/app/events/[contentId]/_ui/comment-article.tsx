'use client';

import React, { useState } from "react";
import { Comment } from "../../../_types/responses/comment-list-response";
import CommentUpdateForm from "./comment-update-form";
import CommentDeleteForm from "./comment-delete-form";

export default function CommentArticle({ comment, contentId, isOwner }: { comment: Comment, contentId: string, isOwner: boolean }) {
  const [isUpdating, setIsUpdating] = useState(false);  

  return (
    <div className="border-t pt-2 text-sm text-gray-800">
      
      {isUpdating ? (
        <CommentUpdateForm
          comment={comment}
          contentId={contentId}
          onCancel={() => setIsUpdating(false)}          
        />
      ) : (
        <>
          <div className="font-semibold">{comment.username}</div>
          {/* 줄바꿈(\n)을 <br />로 변환 */}
          <div>
            {comment.content.split('\n').map((line, i) => (
              <React.Fragment key={i}>
                {line}
                <br />
              </React.Fragment>
            ))}
          </div>          
          <div className="text-xs text-gray-500">
            {comment.createdAt}            
          </div>

          {isOwner && (
            <div className="flex gap-2 mt-1">
              <CommentDeleteForm contentId={contentId} commentId={comment.commentId} />
              <button
                type="button"
                className="px-2 py-1 text-xs bg-gray-200 rounded hover:bg-gray-300"
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