import { Metadata } from "next";
import SignupForm from "./_ui/signup-form";
import Link from "next/link";

export const metadata: Metadata = {
  title: "KoreaEvent: 회원가입",
  description: "KoreaEvent: 회원가입",
};

export default function SignUpPage() {
  return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-[520px] bg-white rounded-xl border border-gray-200 shadow-sm p-10">
        <div className="mb-6 text-center">
          <h1 className="text-[28px] font-semibold tracking-tight text-gray-900 hover:cursor-pointer">
            <Link href="/events">KoreaEvent</Link>
          </h1>
        </div>
        <SignupForm />
        <div className="mt-6 text-center text-sm text-gray-600">
          <span>이미 계정이 있으신가요? </span>
          <Link href="/login" className="font-medium text-blue-400 hover:text-blue-800">
            로그인
          </Link>
        </div>
      </div>
    </main>
  );
}