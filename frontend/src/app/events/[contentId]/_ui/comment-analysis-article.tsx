import { analyzeComment } from '@/app/_libs/fetchers/analyze-comment';
import { fetchCommentCount } from '@/app/_libs/fetchers/fetch-comment-count';

export default async function CommentAnalysisArticle({ contentId }: { contentId: string }) {
  const requiredCommentCount = Number(process.env.NEXT_PUBLIC_REQUIRED_COMMENT_COUNT);

  const { commentCount, error: countError } = await fetchCommentCount(contentId);

  if (countError) {
    return (
      <div className="px-4 py-6 bg-red-50 border border-red-200 rounded-lg">
        <h3 className="text-lg font-semibold text-red-800 mb-2">현재는 댓글의 개수를 불러올 수 없습니다.</h3>
        <p className="text-red-600">{countError.message}</p>
      </div>
    );
  }

  if (commentCount < requiredCommentCount) {
    return (
      <div className="px-4 py-6 bg-yellow-50 border border-yellow-200 rounded-lg space-y-2">
        <h3 className="text-lg font-semibold text-yellow-800"> AI 댓글 분석 대기</h3>
        <p className="text-yellow-700">
          현재 댓글 수: <span className="font-semibold">{commentCount}</span>개
        </p>
        <p className="text-yellow-700">댓글 분석은 댓글이 {requiredCommentCount}개 이상일 때 제공됩니다.</p>
      </div>
    );
  }

  const { commentAnalysisResponse, error } = await analyzeComment(contentId);
  if (error) {
    return (
      <div className="px-4 py-6 bg-red-50 border border-red-200 rounded-lg">        
        <p className="text-red-600">{error.message}</p>
      </div>
    );
  }
  if (!commentAnalysisResponse) {
    return (
      <div className="px-4 py-6 bg-gray-50 rounded-lg">
        <p className="text-gray-600 text-center">현재는 댓글 분석 결과를 불러올 수 없습니다.</p>
      </div>
    );
  }

  const { summary, keywords, emotion } = commentAnalysisResponse;

  const emotionLabelMap: Record<string, string> = {
    positive: "긍정",
    negative: "부정",
    neutral: "중립",
  };
  
  return (
    <div className="px-4 py-6 bg-blue-50 border border-blue-200 rounded-lg space-y-4">
      <h3 className="text-lg font-semibold text-blue-800">💬 AI 댓글 분석 결과</h3>
      
      {/* 요약 */}
      <div className="space-y-2">
        <h4 className="font-medium text-gray-800">📝 요약</h4>
        <p className="text-gray-700 leading-relaxed bg-white p-3 rounded border">
          {summary}
        </p>
      </div>

      {/* 키워드 */}
      <div className="space-y-2">
        <h4 className="font-medium text-gray-800">🔍 주요 키워드</h4>
        <div className="flex flex-wrap gap-2">
          {keywords.map((keyword, index) => (
            <span
              key={index}
              className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium"
            >
              #{keyword}
            </span>
          ))}
        </div>
      </div>

      {/* 감정 분석 */}
      <div className="space-y-3">
        <h4 className="font-medium text-gray-800">😊 감정 분석</h4>
        
        <div className="bg-white p-3 rounded border space-y-3">
          {/* 전체 감정 */}
          <div>
            <span className="text-sm font-medium text-gray-600">전체적인 감정: </span>
            <span className="font-semibold text-gray-800">{emotion.overall}</span>
          </div>

          {/* 주요 감정 */}
          <div>
            <span className="text-sm font-medium text-gray-600">주요 감정: </span>
            <div className="flex flex-wrap gap-2 mt-1">
              {emotion.mainEmotions.map((mainEmotion, index) => (
                <span
                  key={index}
                  className="px-2 py-1 bg-green-100 text-green-800 rounded text-sm"
                >
                  {mainEmotion}
                </span>
              ))}
            </div>
          </div>

          {/* 감정 비율 */}
          <div>
            <span className="text-sm font-medium text-gray-600 block mb-2">감정 비율:</span>
            <div className="space-y-2">
              {Object.entries(emotion.ratio).map(([emotionType, ratio]) => (
                <div key={emotionType} className="flex items-center gap-3">
                  <span className="text-sm w-16 text-gray-700">
                    {emotionLabelMap[emotionType] ?? emotionType}
                  </span>
                  <div className="flex-1 bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-gradient-to-r from-blue-400 to-purple-500 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${ratio}%` }}
                    ></div>
                  </div>
                  <span className="text-sm text-gray-600 w-12 text-right">
                    {ratio.toFixed(1)}%
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 