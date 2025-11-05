'use client';

import { useActionState } from "react";
import { signupAction } from "../../_libs/actions/signup-action";

export default function SignupForm() {
  const [state, action, pending] = useActionState(signupAction, undefined);

  return (
    <form action={action} className="space-y-8 max-w-sm mx-auto">
      <div className="border border-gray-300 rounded-md overflow-hidden">
        <div className="border-b border-gray-200">
          <label htmlFor="email" className="sr-only">이메일</label>
          <input
            id="email"
            name="email"
            placeholder="이메일"
            className="w-full px-4 py-3 text-sm outline-none placeholder-gray-400"
          />
        </div>
        <div className="border-b border-gray-200">
          <label htmlFor="username" className="sr-only">유저명</label>
          <input
            id="username"
            name="username"
            placeholder="유저명"
            className="w-full px-4 py-3 text-sm outline-none placeholder-gray-400"
          />
        </div>
        <div>
          <label htmlFor="password" className="sr-only">비밀번호</label>
          <input
            id="password"
            name="password"
            type="password"
            placeholder="비밀번호"
            className="w-full px-4 py-3 text-sm outline-none placeholder-gray-400"
          />
        </div>
      </div>

      {state?.errors?.email && (
        <ul className="text-red-500 text-sm list-disc pl-5 space-y-1">
          {state.errors.email.map((error: string) => (
            <li key={error}>{error}</li>
          ))}
        </ul>
      )}
      {state?.errors?.username && (
        <ul className="text-red-500 text-sm list-disc pl-5 space-y-1">
          {state.errors.username.map((error: string) => (
            <li key={error}>{error}</li>
          ))}
        </ul>
      )}
      {state?.errors?.password && (
        <ul className="text-red-500 text-sm list-disc pl-5 space-y-1">
          {state.errors.password.map((error: string) => (
            <li key={error}>{error}</li>
          ))}
        </ul>
      )}

      <button
        disabled={pending}
        type="submit"
        className="h-12 w-full rounded-md bg-sky-400 text-white font-medium hover:bg-sky-600 hover:cursor-pointer disabled:opacity-70"
      >
        {pending ? '회원가입 중...' : '회원가입'}
      </button>

      {state?.message && (
        <p className="text-red-500 mt-2 text-sm">{state.message}</p>
      )}
    </form>
  );
}