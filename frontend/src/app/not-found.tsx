import Link from "next/link";

export default function NotFound() {
  return (
    <main className="flex min-h-[60vh] flex-col items-center justify-center px-6 py-10">
      <div className="relative w-full max-w-md overflow-hidden rounded-2xl border bg-background/60 p-8 shadow-lg backdrop-blur">
        <div
          className="pointer-events-none absolute inset-x-0 -top-24 h-40 bg-gradient-to-b from-primary/20 to-transparent blur-2xl"
          aria-hidden="true"
        />
        <div className="flex flex-col items-center text-center">
          <h1 className="text-2xl font-semibold tracking-tight">
            요청하신 페이지를 찾을 수 없습니다
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            주소가 잘못되었거나 페이지가 이동 또는 삭제되었을 수 있습니다.
          </p>          
          <div className="mt-6">
            <button className="rounded-md px-3 py-1.5 text-sm font-medium text-white bg-sky-400 hover:bg-sky-600">
              <Link href="/" aria-label="home">
                메인 페이지로 이동
              </Link>
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}
