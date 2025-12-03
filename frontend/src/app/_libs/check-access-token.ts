import { cookies } from "next/headers";
import { refreshAccessToken } from "./refresh-access-token";

export async function checkAccessToken() {
  const errorStatus = true;

  const cookieStore = await cookies();
  let accessToken = cookieStore.get('access-token')?.value;
  const refreshToken = cookieStore.get('refresh-token')?.value;

  if (!accessToken) {
    if (refreshToken) {      
      const { data, error } = await refreshAccessToken(refreshToken);
      if (error) {
        return { errorStatus };
      }
      if (data) {
        accessToken = data;
      }
    } else {
      return { errorStatus };
    }
  }

  if (!accessToken) {
    return { errorStatus };
  }

  return { accessToken };
} 