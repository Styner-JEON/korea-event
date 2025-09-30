export interface UserResponse {
  id: number;
  name: string;
  role: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiry: number;  
  refreshTokenExpiry: number; 
  user: UserResponse;
}