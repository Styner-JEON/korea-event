import { Metadata } from "next";
import LoginForm from "./_ui/login-form";
import Link from "next/link";

export const metadata: Metadata = {
  title: "KoreaEvent: 로그인",
  description: "KoreaEvent: 로그인",
};

export default function LoginPage() {
  return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-[520px] bg-white rounded-xl border border-gray-200 shadow-sm p-10">
        <div className="mb-6 text-center">
          <h1 className="text-[28px] font-semibold tracking-tight text-gray-900 hover:cursor-pointer">
            <Link href="/events">KoreaEvent</Link>
          </h1>
        </div>
        <LoginForm />
        <div className="mt-6 text-center text-sm text-gray-600">
          <span>계정이 없으신가요? </span>
          <Link href="/signup" className="font-medium text-blue-400 hover:text-blue-800">
            회원가입
          </Link>
        </div>
      </div>
    </main>
  );
}