export default function EmbeddedMap({ title, mapX, mapY}: { title: string, mapX: number, mapY: number }) {  
  const googleMapsUrl = `https://maps.google.com/maps?q=${mapY},${mapX}&hl=ko&z=15&output=embed`;
  
  return (
    <>
      <p>
        <strong>지도</strong>
      </p>
      <div className="h-64">
        <iframe
          src={googleMapsUrl}
          title={`${title} 지도`}
          width="25%"
          height="100%"          
          loading="lazy"
          referrerPolicy="no-referrer"          
          allowFullScreen
        />
      </div>
    </>
  );
} 