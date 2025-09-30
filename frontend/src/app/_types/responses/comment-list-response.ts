export interface CommentListResponse {
  content: Comment[];
  pageable: Pageable;  
  first: boolean;
  last: boolean;
  size: number;
  number: number;
  // totalPages: number;
  // totalElements: number; 
  sort: Sort;
  numberOfElements: number;
  empty: boolean;
}

export interface Comment {
  commentId: number;
  contentId: number;
  userId: number;
  username: string;  
  content: string;  
  createdAt: string;
  updatedAt: string;
}

interface Pageable {
  pageNumber: number;
  pageSize: number;  
  sort: Sort;
  offset: number;
  unpaged: boolean;
  paged: boolean;  
}

interface Sort {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}