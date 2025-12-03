'use client';

import { useActionState } from "react";
import TextareaAutosize from "react-textarea-autosize";
import { Comment } from "../../../_types/responses/comment-list-response";
import { updateCommentAction } from "../../../_libs/actions/update-comment-action";
import { useEffect } from "react";
import { CommentScrollResponse } from "@/app/_types/responses/comment-scroll-response";
import { SWRInfiniteResponse } from "swr/infinite";
import { useRouter } from "next/navigation";

export default function CommentUpdateForm({ comment, contentId, onCancel, commentMutate }: {
  comment: Comment;
  contentId: string;
  onCancel: () => void;
  commentMutate: SWRInfiniteResponse<CommentScrollResponse>["mutate"];
}) {
  const router = useRouter();

  const bindedUpdateComment = updateCommentAction.bind(null, comment, contentId);
  const [state, action, pending] = useActionState(bindedUpdateComment, undefined);

  const commentResponse = state?.commentResponse;

  useEffect(() => {
    if (commentResponse) {
      onCancel();

      commentMutate(
        (commentScrollResponseList) => {
          if (!Array.isArray(commentScrollResponseList) || commentScrollResponseList.length === 0) {
            return [
              {
                commentResponseList: [commentResponse],
                nextCursor: null,
              },
            ];
          }

          const removedCommentScrollResponseList = commentScrollResponseList.map((commentScrollResponse: CommentScrollResponse) => ({
            ...commentScrollResponse,
            commentResponseList: commentScrollResponse.commentResponseList.filter((c) =>
              c.commentId !== commentResponse.commentId
            )
          }));

          return removedCommentScrollResponseList.map((commentScrollResponse, index) =>
            index === 0
              ? {
                ...commentScrollResponse,
                commentResponseList: [
                  commentResponse,
                  ...commentScrollResponse.commentResponseList,
                ],
              }
              : commentScrollResponse
          );
        },
        {
          revalidate: false
        }
      );
    }
  }, [commentResponse, onCancel, commentMutate]);

  useEffect(() => {
    if (state?.beforeLoginMessage) {
      const timer = setTimeout(() => {
        router.push('/login');
      }, 1500);

      return () => clearTimeout(timer);
    }
  }, [state?.beforeLoginMessage, router]);

  return (
    <form action={action} className="border-2 border-sky-400 rounded-md">
      <TextareaAutosize
        name="content"
        minRows={2}
        maxRows={8}
        placeholder="리뷰를 작성할 수 있습니다"
        className="p-4 w-full resize-none focus:outline-none"
        disabled={pending}
        defaultValue={comment.content}
      />
      <div className="flex gap-2 m-2">
        <button
          type="button"
          onClick={onCancel}
          disabled={pending}
          className="rounded-md border p-2 transition-colors hover:bg-gray-100 hover:text-gray-700 hover:cursor-pointer"
        >
          수정 취소
        </button>
        <button
          type="submit"
          disabled={pending}
          className="rounded-md border p-2 transition-colors bg-gray-200 hover:bg-gray-300 hover:cursor-pointer"
        >
          {pending ? '수정 중…' : '댓글 수정'}
        </button>
      </div>

      {state?.validationError?.content && <p>{state.validationError.content}</p>}
      {state?.message && (
        <p className="text-red-500 mt-2">{state.message}</p>
      )}
      {state?.beforeLoginMessage && (
        <p className="text-red-500 mt-2">{state.beforeLoginMessage}</p>
      )}
    </form>
  );
}