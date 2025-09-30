'use client';

import { useActionState, useEffect, useState } from 'react';
import TextareaAutosize from 'react-textarea-autosize';
import { insertCommentAction } from '../../../_libs/actions/insert-comment-action';
import { useRouter } from 'next/navigation';
import { SWRInfiniteResponse } from 'swr/infinite';
import { CommentScrollResponse } from '@/app/_types/responses/comment-scroll-response';

export default function CommentInsertForm({ contentId, isLoggedIn, commentMutate }: { 
  contentId: string; 
  isLoggedIn: boolean;
  commentMutate: SWRInfiniteResponse<CommentScrollResponse>["mutate"];
}) {    
  const router = useRouter();
  const [content, setContent] = useState('');
  
  const bindedCreateComment = insertCommentAction.bind(null, contentId);
  const [state, action, pending] = useActionState(bindedCreateComment, undefined);

  const commentResponse = state?.commentResponse;

  useEffect(() => {
    if (commentResponse) {      
      commentMutate(
        (commentScrollResponseList) => {
          if (!commentScrollResponseList || commentScrollResponseList.length === 0) {      
            return [{
              commentResponseList: [commentResponse],
              nextCursor: null
            }];
          }
      
          const newCommentScrollResponseList = [...commentScrollResponseList];
          newCommentScrollResponseList[0] = {
            ...newCommentScrollResponseList[0],
            commentResponseList: [commentResponse, ...newCommentScrollResponseList[0].commentResponseList]
          };

          return newCommentScrollResponseList;
        },
        {
          revalidate: false
        }
      );

      setContent('');    
    }
  }, [commentResponse, commentMutate]);

  const handleFocus = () => {
    if (!isLoggedIn) {
      router.push('/login');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setContent(e.target.value);
  };

  return (   
    <form action={action} className="border border-gray-400 rounded-md w-120">      
      <TextareaAutosize
        name="content"
        minRows={2}
        maxRows={8}
        placeholder="리뷰를 작성할 수 있습니다"
        className="p-4 w-full resize-none focus:outline-none"
        disabled={pending}
        onFocus={handleFocus}
        value={content}
        onChange={handleChange}
      />
      <div className="flex items-center justify-between px-4 pb-1">
        <span className="text-xs text-gray-500">{content.length}/1000</span>
        <button 
          type="submit"
          disabled={pending}
          className="px-4 py-2 bg-blue-400 text-white hover:bg-purple-400 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {pending ? '작성 중…' : '댓글 작성'}
        </button>
      </div>
      {state?.validationError?.content && <p>{state.validationError.content}</p>}
      {state?.message && (
        <p className="text-red-500 mt-2">{state.message}</p>
      )}
    </form>
  );
}