'use client';
 
import { useEffect } from 'react';
 
export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {    
    console.error('[General ERROR]', error);
  }, [error]);
 
  return (
    <main className="flex h-full flex-col items-center justify-center">
      <h2 className="text-center">현재는 접속을 할 수 없습니다. 조금 뒤에 다시 시도해주세요.</h2>
      <button
        className="mt-4 rounded-md bg-sky-400 px-4 py-2 text-sm text-white transition-colors hover:bg-sky-600"
        onClick={          
          () => reset()
        }
      >
        Try again
      </button>
    </main>
  );
}