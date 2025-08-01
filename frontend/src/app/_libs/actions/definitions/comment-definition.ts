import { z } from "zod";

export const CommentSchema = z.object({  
  content: z
    .string()
    .min(1, { message: '내용은 1자 이상이어야 합니다.' })
    .max(1000, { message: '내용은 1000자 이하여야 합니다.' })
});

export type CommentState = 
  | {
      validationError?: { 
        content?: string[];       
      };
      message?: string;
      status?: number;
      code?: unknown;      
    }
  | undefined;
