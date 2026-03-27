import type { Metadata } from "next";
import "./globals.css";

const SITE_URL = "https://www.koreaevent.click";
const SITE_TITLE = "KoreaEvent: 대한민국 행사와 축제 정보";
const SITE_DESCRIPTION =
  "지역/키워드로 대한민국 행사와 축제 정보를 빠르게 찾아보세요.";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: SITE_TITLE,
  description: SITE_DESCRIPTION,
  openGraph: {
    title: SITE_TITLE,
    description: SITE_DESCRIPTION,
    url: SITE_URL,
    siteName: "KoreaEvent",
    locale: "ko_KR",
    type: "website",
  },
  twitter: {
    card: "summary",
    title: SITE_TITLE,
    description: SITE_DESCRIPTION,
  },
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