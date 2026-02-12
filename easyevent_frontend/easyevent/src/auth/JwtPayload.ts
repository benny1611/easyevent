export interface JwtPayload {
  sub: string;
  iat: number;
  exp: number;
  roles: string[];
  profilePictureUrl: string | null;
  username: string;
}