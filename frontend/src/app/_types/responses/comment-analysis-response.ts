export interface CommentAnalysisResponse {
  summary: string;
  keywords: string[];
  emotion: CommentEmotion;
}
export interface CommentEmotion {
  overall: string;
  ratio: Record<string, number>;
  mainEmotions: string[];
}