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
          if (!Array.isArray(commentScrollResponseList) || commentScrollResponseList.length === 0) {
            return [
              {
                commentResponseList: [commentResponse],
                nextCursor: null,
              },
            ];
          }

          return commentScrollResponseList.map((commentScrollResponse, index) =>
            index === 0
              ? {
                ...commentScrollResponse,
                commentResponseList: [
                  commentResponse,
                  ...commentScrollResponse.commentResponseList
                ],
              }
              : commentScrollResponse
          );
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

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    if (!isLoggedIn) {
      e.preventDefault();
      router.push('/login');
    }
  };

  return (
    <form 
      action={action} 
      onSubmit={handleSubmit} 
      className="rounded-xl border border-gray-200 bg-white shadow-sm"
    >
      <div className="p-4">
        <TextareaAutosize
          name="content"
          minRows={3}
          maxRows={8}
          placeholder="리뷰를 작성할 수 있습니다"
          className="w-full resize-none rounded-md border-none focus:outline-none focus:ring-0"
          disabled={pending}
          onFocus={handleFocus}
          value={content}
          onChange={handleChange}
        />
        <div className="mt-2 flex items-end justify-between">
          <span className="text-xs text-gray-500">{content.length}/1000</span>
          <button
            type="submit"
            disabled={pending}
            className="px-4 py-2 bg-sky-500 text-white hover:bg-sky-600 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {pending ? '작성 중…' : '댓글 작성'}
          </button>
        </div>

        {state?.validationError?.content && <p className="text-xs text-red-500 mt-2">{state.validationError.content}</p>}
        {state?.message && (
          <p className="text-red-500 mt-2">{state.message}</p>
        )}
      </div>
    </form>
  );
}