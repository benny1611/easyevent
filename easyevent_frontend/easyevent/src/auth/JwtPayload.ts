export interface JwtPayload {
  sub: string; 
  iat: number;
  exp: number; 
  roles: string[];
}