import { Suspense } from "react";
import ComponentLoading from "../../_ui/component-loading";
import FavoriteMain from "./_ui/favorite-main";

export default function FavoritesPage({ searchParams }: {
  searchParams?: Promise<{
    page?: string;
    // query?: string;
    // area?: string;
  }>;
}) {
  return (
    <div className="mx-auto max-w-6xl px-6 py-8 space-y-8">
      {/* <Suspense fallback={<ComponentLoading />}>
        <Search placeholder="검색어를 입력할 수 있습니다." />
      </Suspense> */}
      {/* <Suspense fallback={<ComponentLoading />}>
        <AreaSelector />
      </Suspense> */}
      <Suspense fallback={<ComponentLoading />}>
        <FavoriteMain searchParams={searchParams} />
      </Suspense>
    </div>
  );
}