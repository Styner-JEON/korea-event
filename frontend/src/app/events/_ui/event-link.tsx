import { EventList } from "../../_types/responses/event-list-response";
import Image from "next/image";
import Link from "next/link";

export default function EventLink({ eventList }: { eventList: EventList }) {
  return (
    <Link href={`/events/${eventList.contentId}`} className="block w-60">
      <p className="relative w-60 h-40">
        {eventList.firstImage && (
          <Image 
            src={eventList.firstImage}
            alt={eventList.title}        
            fill={true}
            className="object-cover"
          />
        )}
      </p>
      <p>{eventList.title}</p>
      <p>{eventList.eventStartDate} ~ {eventList.eventEndDate}</p>      
    </Link>
  );
}
