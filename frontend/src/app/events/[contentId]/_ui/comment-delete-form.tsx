'use client';

import { useActionState, useEffect } from "react";
import { deleteCommentAction } from "../../../_libs/actions/delete-comment-action";
import { SWRInfiniteResponse } from "swr/infinite";
import { CommentScrollResponse } from "@/app/_types/responses/comment-scroll-response";

export default function CommentDeleteForm({ contentId, commentId, commentMutate }: {
  contentId: string;
  commentId: number;
  commentMutate: SWRInfiniteResponse<CommentScrollResponse>["mutate"];
}) {
  const bindedDeleteCommentAction = deleteCommentAction.bind(null, contentId, commentId);
  const [state, action, pending] = useActionState(bindedDeleteCommentAction, undefined);

  const commentResponse = state?.commentResponse;

  useEffect(() => {
    if (commentResponse) {
      commentMutate(
        (commentScrollResponseList) => {
          if (!Array.isArray(commentScrollResponseList) || commentScrollResponseList.length === 0) {
            return commentScrollResponseList ?? [];
          }

          return commentScrollResponseList.map((commentScrollResponse: CommentScrollResponse) => ({
            ...commentScrollResponse,
            commentResponseList: commentScrollResponse.commentResponseList.filter((c) =>
              c.commentId !== commentId
            )
          }));
        },
        {
          revalidate: false
        }
      );
    }
  }, [commentResponse, commentMutate, commentId]);

  return (
    <form action={action}>
      <button
        type="submit"
        disabled={pending}
        className={`rounded-md border p-2 transition-colors ${pending
          ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
          : 'hover:bg-gray-100 hover:text-gray-700 hover:cursor-pointer'
          }`}
      >
        <p>{pending ? '삭제 중...' : '삭제'}</p>
      </button>
      {state?.message && (
        <p className="text-red-500 text-sm">{state.message}</p>
      )}
    </form>
  );
}
