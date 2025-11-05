import { CommentAnalysisResponse } from "@/app/_types/responses/comment-analysis-response";
import { ErrorResponse } from "../../_types/responses/error-response";

export async function analyzeComment(contentId: string): Promise<{
  commentAnalysisResponse?: CommentAnalysisResponse;
  error?: ErrorResponse;
}> {
  const message = '지금은 댓글 분석 결과를 불러올 수 없습니다.';
  const url = `${process.env.NEXT_PUBLIC_AI_BASE_URL}/ai/${process.env.NEXT_PUBLIC_AI_API_VERSION}/${contentId}/analysis`;
  let response: Response;

  // const revalidateSeconds = Number(process.env.NEXT_PUBLIC_COMMENT_ANALYSIS_REVALIDATE_SECONDS);
  
  try {
    response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },      
      next: {        
        // revalidate: revalidateSeconds,
        tags: [`analysis:${contentId}`],
      },
    });
  } catch (error) {
    console.error('[Network ERROR]', error);
    return { error: new Error(message) };
  }

  if (!response.ok) {
    let errorJson: ErrorResponse;
    try {
      errorJson = await response.json();
    } catch (error) {
      console.error('[Error Response Parsing ERROR]', error);
      return { error: new Error(message) };
    }

    let detailedMessage = message;
    const httpStatus = response.status;
    switch (httpStatus) {
      case 404:
        detailedMessage = '요청하신 데이터를 찾을 수 없습니다.'; 
        break;
      case 500:
        detailedMessage = '서버 에러가 발생했습니다. 잠시 후 다시 시도해 주세요.'; 
        break;
    }
    console.error('[Backend ERROR]', httpStatus, errorJson.message);
    return { error: new Error(detailedMessage) };
  }

  let responseJson: CommentAnalysisResponse;
  try {
    responseJson = await response.json();
  } catch (error) {
    console.error('[Response Parsing ERROR]', error);
    return { error: new Error(message) };
  }

  return { commentAnalysisResponse: responseJson};
}