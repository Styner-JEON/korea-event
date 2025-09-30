import { CommentResponse } from "./comment-response";

export interface CommentScrollResponse {
  commentResponseList: CommentResponse[];
  nextCursor: string | null;
}