import { fetchEventList } from "../../_libs/fetchers/fetch-event-list";
import EventLink from "./event-link";
import { EventList } from "../../_types/responses/event-list-response";
import MainPagination from "./main-pagination";

export default async function Main(props: {
  searchParams?: Promise<{
    page?: string;
    query?: string;
    area?: string;
  }>;
}) {
  const searchParams = await props.searchParams;
  const pageNumber = Number(searchParams?.page) || 0;
  const query = searchParams?.query || '';
  const area = searchParams?.area || '';

  const { eventListResponse, error } = await fetchEventList(pageNumber, query, area);
  // console.log('eventListResponse', eventListResponse);

  if (error) {
    return <div className="text-red-500 p-4">{error.message}</div>;
  }

  return (
    <main className="mx-auto max-w-6xl px-6">
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