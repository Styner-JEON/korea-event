'use client';

import { useActionState } from "react";
import { deleteCommentAction } from "../../../_libs/actions/delete-comment-action";

export default function CommentDeleteForm({ contentId, commentId }: { contentId: string, commentId: number }) {    
  const bindedDleteCommentAction = deleteCommentAction.bind(null, contentId, commentId);
  const [state, action, pending] = useActionState(bindedDleteCommentAction, undefined);

  return (    
    <form action={action}>
      <button 
        type="submit" 
        disabled={pending} 
        className={`rounded-md border p-2 transition-colors ${
          pending 
            ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
            : 'hover:bg-gray-100 hover:text-gray-700'
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
