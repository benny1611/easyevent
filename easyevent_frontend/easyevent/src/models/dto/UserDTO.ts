export class UserDTO {
    email: string;
    name: string;
    profilePicture: string|null;
    language: string|null;
    oldPassword: string|null;
    newPassword: string|null;
    token: string|null;
    active: boolean;
    isPasswordSet: boolean;

    constructor(email: string, name: string, profilePicture: string|null, language: string|null, oldPassword: string|null, newPassword: string|null, token: string|null, active: boolean, isPasswordSet: boolean) {
        this.email = email;
        this.name = name;
        this.profilePicture = profilePicture;
        this.language = language;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.token = token;
        this.active = active;
        this.isPasswordSet = isPasswordSet;
    }
}

export default UserDTO;