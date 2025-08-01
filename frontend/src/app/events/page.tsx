import { Suspense } from "react";
import Search from "./_ui/search";
import Main from "./_ui/main";
import ComponentLoading from "../_ui/component-loading";
import AreaSelector from "./_ui/area-selector";

export default function EventsPage(props: {
  searchParams?: Promise<{
    page?: string;
    query?: string;
    area?: string;
  }>;
}) {
  return (    
    <div className="space-y-8">
      <Search placeholder="검색어를 입력할 수 있습니다." />
      <AreaSelector />
      <Suspense fallback={<ComponentLoading />}>
        <Main searchParams={props.searchParams} />    
      </Suspense>
    </div>
  );
}