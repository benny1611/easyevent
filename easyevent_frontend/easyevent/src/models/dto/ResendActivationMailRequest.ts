export class ResendActivationMailRequest {
    token: string;

    constructor(token: string) {
        this.token = token;
    }
}