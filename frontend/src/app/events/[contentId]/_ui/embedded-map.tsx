export default function EmbeddedMap({ title, mapX, mapY }: { title: string, mapX: number, mapY: number }) {
  const googleMapsUrl = `https://maps.google.com/maps?q=${mapY},${mapX}&hl=ko&z=15&output=embed`;

  return (
    <div className="w-full h-72 md:h-80 lg:h-96">
      <iframe
        src={googleMapsUrl}
        title={`${title} 지도`}
        width="100%"
        height="100%"
        loading="lazy"
        referrerPolicy="no-referrer"
        allowFullScreen
        className="w-full h-full border-0"
      />
    </div>
  );
} 