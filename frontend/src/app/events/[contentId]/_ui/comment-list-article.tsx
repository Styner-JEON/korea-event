import React from "react";
import { fetchCommentList } from "../../../_libs/fetchers/fetch-comment-list";
import { Comment } from "../../../_types/responses/comment-list-response";
import CommentArticle from "./comment-article";

export default async function CommentListArticle({ contentId, username }: { contentId: string, username?: string }) {
  const { commentListResponse, error } = await fetchCommentList(contentId);
  
  if (error) {
    return <div className="text-red-500 p-4">{error.message}</div>;
  }

  return (    
    <article className="px-4 pb-4 space-y-2">      
      {commentListResponse?.content.map((comment: Comment) => (
        <CommentArticle
          key={comment.commentId}
          comment={comment}
          contentId={contentId}
          isOwner={username === comment.username}
        />
      ))}
    </article>
  );
}

// import React from "react";
// import { fetchCommentList } from "../_libs/fetch-comment-list";
// import { Comment } from "../_types/comment-list-response";
// import CommentDeleteForm from "./comment-delete-form";
// import CommentUpdateForm from "./comment-update-form";

// export default async function CommentListArticle({ contentId, username }: { contentId: string, username?: string }) {
//   const { commentListResponse, error } = await fetchCommentList(contentId);
  
//   if (error) {
//     return <div className="text-red-500 p-4">{error.message}</div>;
//   }

//   return (    
//     <article className="px-4 pb-4 space-y-2">      
//       {commentListResponse?.content.map((comment: Comment) => (
//         <div
//           key={comment.commentId}
//           className="border-t pt-2 text-sm text-gray-800"
//         >
//           <div className="font-semibold">{comment.username}</div>
          
//           {/* <div>{comment.content}</div> */}
//           {/* 줄바꿈(\n)을 <br />로 변환 */}
//           <div>
//             {comment.content.split('\n').map((line, i) => (
//               <React.Fragment key={i}>
//                 {line}
//                 <br />
//               </React.Fragment>
//             ))}
//           </div>

//           <div className="text-xs text-gray-500">
//             {new Date(comment.createdAt).toLocaleString()}
//           </div>

//           {username === comment.username && (
//             <>
//               <CommentDeleteForm commentId={comment.commentId} contentId={contentId} />            
//               {/* <button>수정</button> */}
//               <CommentUpdateForm comment={comment} contentId={contentId} />              
//             </>
//           )}
//         </div>
//       ))}
//     </article>
//   );
// }




// 'use client';

// import useSWR from "swr";
// import { fetchCommentList } from "../_libs/fetch-comment-list";
// import { Comment } from "../_types/comment-list-response";

// export default function CommentListArticle({ contentId }: { contentId: string }) {
//   const url = `${process.env.NEXT_PUBLIC_EVENT_BASE_URL}/events/${process.env.NEXT_PUBLIC_EVENT_API_VERSION}/${contentId}/comments`;  
//   const { data, error, isLoading } = useSWR(url, fetchCommentList);

//   if (isLoading) {
//     return <p className="text-gray-400 text-sm">댓글을 불러오는 중...</p>;
//   }

//   if (error) {
//     return <p className="text-red-500 text-sm">지금은 댓글을 불러올 수 없습니다.</p>;
//   }

//   return (    
//     <article className="px-4 pb-4 space-y-2">      
//       {data?.content.map((comment: Comment) => (
//           <div
//             key={comment.commentId}
//             className="border-t pt-2 text-sm text-gray-800"
//           >
//             <div className="font-semibold">{comment.username}</div>
//             <div>{comment.content}</div>
//             <div className="text-xs text-gray-500">
//               {new Date(comment.createdAt).toLocaleString()}
//             </div>
//           </div>
//       ))}
//     </article>
//   );
// }


