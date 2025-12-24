'use client';

import { useActionState, useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { signupAction } from "../../_libs/actions/signup-action";

export default function SignupForm() {
  const [state, action, pending] = useActionState(signupAction, undefined);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

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
        <div className="border-b border-gray-200">
          <label htmlFor="password" className="sr-only">비밀번호</label>
          <div className="relative">
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
        <div>
          <label htmlFor="confirmPassword" className="sr-only">비밀번호 확인</label>
          <div className="relative">
            <input
              id="confirmPassword"
              name="confirmPassword"
              type={showConfirmPassword ? "text" : "password"}
              placeholder="비밀번호 확인"
              className="w-full px-4 py-3 pr-12 text-sm outline-none placeholder-gray-400"
            />
            <button
              type="button"
              aria-label={showConfirmPassword ? "비밀번호 확인 숨기기" : "비밀번호 확인 보기"}
              onClick={() => setShowConfirmPassword((v) => !v)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
            >
              {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
            </button>
          </div>
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
      {state?.errors?.confirmPassword && (
        <ul className="text-red-500 text-sm list-disc pl-5 space-y-1">
          {state.errors.confirmPassword.map((error: string) => (
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