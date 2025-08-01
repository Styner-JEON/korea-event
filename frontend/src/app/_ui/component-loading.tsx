export default function ComponentLoading() {
  return (
    <div className="flex justify-center items-center p-8">
      <div className="relative w-12 h-12">
        <style dangerouslySetInnerHTML={{
          __html: `
            @keyframes radial-fade {
              0%, 39%, 100% { opacity: 0.3; }
              40% { opacity: 1; }
            }
            .radial-bar {
              animation: radial-fade 1.2s linear infinite;
            }
          `
        }} />
        
        {/* 8개의 막대를 360도로 배치 */}
        {Array.from({ length: 8 }, (_, i) => (
          <div
            key={i}
            className="absolute w-1 h-4 bg-blue-500 rounded-full radial-bar"
            style={{
              left: '50%',
              top: '50%',
              transformOrigin: '0.125rem 1.8rem',
              transform: `translate(-50%, -100%) rotate(${i * 45}deg)`,
              animationDelay: `${i * 0.15}s`,
            }}
          />
        ))}
      </div>
    </div>
  );
}