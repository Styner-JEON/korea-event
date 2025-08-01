export interface RefreshAccessTokenResponse {
  accessToken: string;  
  accessTokenExpiry: number;
  user: UserResponse;
}

interface UserResponse {
  id: number;
  name: string;
  role: string;
}

export interface ErrorResponse { 
  message: string;
};