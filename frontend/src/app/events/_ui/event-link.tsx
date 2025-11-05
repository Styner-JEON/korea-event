import { EventList } from "../../_types/responses/event-list-response";
import Image from "next/image";
import Link from "next/link";
import { formatDateToDot } from "@/libs/utils";

export default function EventLink({ eventList }: { eventList: EventList }) {
  return (
    <Link
      href={`/events/${eventList.contentId}`}
      className="block w-full rounded-lg overflow-hidden border border-gray-200 bg-white shadow-sm hover:shadow-2xl transition-shadow"
    >
      <p className="relative w-full h-40 bg-gray-100">
        {eventList.firstImage && (
          <Image
            src={eventList.firstImage}
            alt={eventList.title}
            fill={true}
            sizes="(min-width: 240px) 240px, 100vw"
            className="object-cover"
          />
        )}
      </p>
      <div className="p-3">
        <p className="text-sm font-medium text-gray-800 line-clamp-2 min-h-[40px]">
          {eventList.title}
        </p>
        <p className="mt-2 text-xs text-gray-500">
          {formatDateToDot(eventList.eventStartDate)} ~ {formatDateToDot(eventList.eventEndDate)}
        </p>
      </div>
    </Link>
  );
}
