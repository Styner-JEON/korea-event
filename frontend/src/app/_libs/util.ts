import { CommentScrollResponse } from "../_types/responses/comment-scroll-response";

export const  generatePagination = (currentPage: number, totalPages: number) => {
  const BLOCK_SIZE = Number(process.env.NEXT_PUBLIC_PAGINATION_BLOCK_SIZE);
  const blockNumber = Math.floor(currentPage / BLOCK_SIZE);
  const startPage = blockNumber * BLOCK_SIZE;
  const endPage = Math.min((blockNumber + 1) * BLOCK_SIZE - 1, totalPages - 1); 

  return {
    allPages: Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i),
    startPage,
    endPage,
  };
}

export const createCommentListGetKey = (contentId: string) => (
  pageIndex: number,
  previousPageData: CommentScrollResponse | null
): [string, number] | null => {  
  if (previousPageData) {
    if (previousPageData.nextCursor === null) {      
      return null;
    } else {      
      return [`/${contentId}/comments?cursor=${previousPageData.nextCursor}`, pageIndex + 1];
    }
  } else if (pageIndex === 0) {    
    return [`/${contentId}/comments`, pageIndex + 1]; 
  } 

  return null;
}