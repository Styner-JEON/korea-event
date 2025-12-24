'use client';

import { useActionState, useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { loginAction } from "../../_libs/actions/login-action";

export default function LoginForm() {
  const [state, action, pending] = useActionState(loginAction, undefined);
  const [showPassword, setShowPassword] = useState(false);

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
        <div className="relative">
          <label htmlFor="password" className="sr-only">비밀번호</label>
          <input
            id="password"
            name="password"
            type={showPassword ? "text" : "password"}
            placeholder="비밀번호"
            className="w-full px-4 py-3 pr-12 text-sm outline-none placeholder-gray-400"
          />
          <button
            type="button"
            aria-label={showPassword ? "비밀번호 숨기기" : "비밀번호 보기"}
            onClick={() => setShowPassword((v) => !v)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
          >
            {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {state?.errors?.email && (
        <ul className="text-red-500 text-sm list-disc pl-5 space-y-1">
          {state.errors.email.map((error: string) => (
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
        {pending ? '로그인 중...' : '로그인'}
      </button>

      {state?.message && (
        <p className="text-red-500 mt-2 text-sm">{state.message}</p>
      )}
    </form>
  );
}