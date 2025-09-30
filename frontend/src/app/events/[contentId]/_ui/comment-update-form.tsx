'use client';

import { useActionState } from "react";
import TextareaAutosize from "react-textarea-autosize";
import { Comment } from "../../../_types/responses/comment-list-response";
import { updateCommentAction } from "../../../_libs/actions/update-comment-action";
import { useEffect } from "react";
import { CommentScrollResponse } from "@/app/_types/responses/comment-scroll-response";
import { SWRInfiniteResponse } from "swr/infinite";

export default function CommentUpdateForm({ comment, contentId, onCancel, commentMutate }: { 
  comment: Comment;
  contentId: string; 
  onCancel: () => void;
  commentMutate: SWRInfiniteResponse<CommentScrollResponse>["mutate"];
}) {
  const bindedUpdateComment = updateCommentAction.bind(null, comment, contentId);
  const [state, action, pending] = useActionState(bindedUpdateComment, undefined);  

  const commentResponse = state?.commentResponse;

  useEffect(() => {
    if (commentResponse) {
      onCancel();
      
      commentMutate(
        (commentScrollResponseList) => {
          if (!commentScrollResponseList) {
            return commentScrollResponseList;
          }

          return commentScrollResponseList.map((commentScrollResponse: CommentScrollResponse) => ({
            ...commentScrollResponse,
            commentResponseList: commentScrollResponse.commentResponseList.map((existingComment) =>
              existingComment.commentId === comment.commentId
                ? { ...existingComment, ...commentResponse }
                : existingComment
            )
          }));
        },
        {
          revalidate: false
        }
      );
    }   
  }, [commentResponse, onCancel, commentMutate, comment.commentId]);
  
  return (   
    <form action={action} className="border border-gray-400 rounded-md w-120">      
      <TextareaAutosize
        name="content"
        minRows={2}
        maxRows={8}
        placeholder="리뷰를 작성할 수 있습니다"
        className="p-4 w-full resize-none focus:outline-none" 
        disabled={pending}        
        defaultValue={comment.content}        
      />
      <button 
        type="submit"
        disabled={pending}
        className="px-4 py-2 bg-blue-400 text-white hover:bg-purple-400 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {pending ? '수정 중…' : '댓글 수정'}
      </button>            
      <button
        type="button"
        onClick={onCancel}
        disabled={pending}
        className="px-4 py-2 bg-gray-200 text-gray-700 hover:bg-gray-300 rounded-md"
      >
        수정 취소
      </button>

      {state?.validationError?.content && <p>{state.validationError.content}</p>}
      {state?.message && (
        <p className="text-red-500 mt-2">{state.message}</p>
      )}
    </form>
  );
}