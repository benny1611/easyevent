export class JwtResponse {
    token: string;

    constructor(token: string) {
        this.token = token;
    }
}

export default JwtResponse;