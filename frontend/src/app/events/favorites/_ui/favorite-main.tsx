import { fetchFavoriteEventList } from "@/app/_libs/fetchers/fetch-favorite-event-list";
import MainPagination from "../../_ui/main-pagination";
import EventLink from "../../_ui/event-link";
import { EventList } from "@/app/_types/responses/event-list-response";

export default async function FavoriteMain(props: {
  searchParams?: Promise<{
    page?: string;
    // query?: string;
    // area?: string;
  }>;
}) {
  const searchParams = await props.searchParams;
  const pageNumber = Number(searchParams?.page) || 0;
  // const query = searchParams?.query || '';
  // const areaString = searchParams?.area || '';

  // const { eventListResponse, error } = await fetchFavoriteEventList(pageNumber, query, areaString);
  const { eventListResponse, error } = await fetchFavoriteEventList(pageNumber);
  // console.log('eventListResponse', eventListResponse);

  if (error) {
    return <div className="text-red-500 p-4">{error.message}</div>;
  }

  return (
    <main className="mx-auto max-w-6xl px-6">
      <h1 className="text-lg font-semibold text-gray-900 mb-8">
        즐겨찾기한 이벤트
      </h1>
      <nav className="mb-8">
        <ul className="grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-6">
          {eventListResponse?.content.map((eventList: EventList) => (
            <li key={eventList.contentId}>
              <EventLink eventList={eventList} />
            </li>
          ))}
        </ul>
      </nav>
      <MainPagination totalPages={eventListResponse?.totalPages ?? 0} />
    </main>
  );
}