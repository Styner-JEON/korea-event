import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  metadataBase: new URL('https://www.koreaevent.click'),
  title: 'KoreaEvent: 대한민국 행사와 축제 정보',
  description: '지역/키워드로 대한민국 행사와 축제 정보를 빠르게 찾아보세요.',
  other: {
    'naver-site-verification': '12daf7e886c00a596c9d4cc3b8efe24418c59430',    
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="">
        {children}
      </body>
    </html>
  );
}