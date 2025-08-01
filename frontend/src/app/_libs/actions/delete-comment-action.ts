'use server';

import { checkAccessToken } from "@/app/_libs/check-access-token";
import { revalidatePath } from "next/cache";

export async function deleteCommentAction(contentId: string, commentId: number) {
  const beforeLoginMessage = '댓글을 삭제하려면 로그인이 필요합니다.';
  const { accessToken, isError } = await checkAccessToken();
  if (isError) {
    return { beforeLoginMessage };
  }

  const message = '지금은 댓글을 삭제할 수 없습니다. 잠시 후 다시 시도해주세요.';
  const url = `${process.env.NEXT_PUBLIC_EVENT_BASE_URL}/events/${process.env.NEXT_PUBLIC_EVENT_API_VERSION}/${contentId}/comments/${commentId}`;
  
  let response: Response;

  try {    
    response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      cache: 'no-store',
    });    
  } catch (error) {
    console.error('[Network ERROR]', error);
    return { message };
  }

  if (!response.ok) {
    let errorJson;
    try {
      errorJson = await response.json();      
    } catch (error) {
      console.error('[Error Response Parsing ERROR]', error);
      return { message };
    }

    let detailedMessage = message;
    const httpStatus = response.status;
    switch (httpStatus) {
      case 404:
        detailedMessage = '요청하신 데이터를 찾을 수 없습니다.'; 
        break;
      case 401:
        detailedMessage = '삭제 권한이 필요합니다.'; 
        break;
      case 409:
        detailedMessage = '삭제 권한이 필요합니다.'; 
        break;
      case 500:
        detailedMessage = '서버 에러가 발생했습니다. 잠시 후 다시 시도해 주세요.'; 
        break;
    }

    console.error('[Backend ERROR]', httpStatus, errorJson.message);
    return { message: detailedMessage };      
  }  

  console.log('[댓글 삭제 완료]');
  revalidatePath(`/events/${contentId}`, 'page');  
  // redirect(`/events/${contentId}`);

  return { success: true };
}

