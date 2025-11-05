import { z } from 'zod';
 
export const SignupFormSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, { message: '이메일을 입력하세요.' })
    .email({ message: '잘못된 이메일 형식입니다.' }),
  username: z
    .string()
    .trim()
    .min(4, { message: '유저명은 4자 이상이어야 합니다.' })
    .max(16, { message: '유저명은 16자 이하여야 합니다.' })    
    .refine((v) => !/\s/.test(v), { message: '유저명에는 공백을 사용할 수 없습니다.' }),
  password: z
    .string()
    .trim()
    .min(8, { message: '비밀번호는 8글자 이상이어야 합니다.' })
    .max(16, { message: '비밀번호는 16자 이하여야 합니다.' })
    .regex(/[a-zA-Z]/, { message: '비밀번호는 1개 이상의 문자를 포함해야 합니다.' })
    .regex(/[0-9]/, { message: '비밀번호는 1개 이상의 숫자를 포함해야 합니다.' })
    .regex(/[^a-zA-Z0-9]/, { message: '비밀번호는 1개 이상의 특수문자를 포함해야 합니다.' })
    .refine((v) => !/\s/.test(v), { message: '비밀번호에는 공백을 사용할 수 없습니다.' }),
});

export type SignupFormState = 
  | {
      errors?: { 
        email?: string[]; 
        username?: string[];         
        password?: string[];
      };
      message?: string;
    }
  | undefined;

