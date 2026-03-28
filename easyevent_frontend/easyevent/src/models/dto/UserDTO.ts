export class UserDTO {
    email: string;
    name: string;
    profilePicture: string|null;
    language: string|null;
    oldPassword: string|null;
    newPassword: string|null;
    token: string|null;
    id: number|null;
    active: boolean;
    isLocalPasswordSet: boolean;
    isAdmin: boolean;

    constructor(email: string, name: string, profilePicture: string|null, language: string|null, oldPassword: string|null, newPassword: string|null, token: string|null, id: number|null, active: boolean, isLocalPasswordSet: boolean, isAdmin: boolean) {
        this.email = email;
        this.name = name;
        this.profilePicture = profilePicture;
        this.language = language;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.token = token;
        this.id = id;
        this.active = active;
        this.isLocalPasswordSet = isLocalPasswordSet;
        this.isAdmin = isAdmin;
    }
}

export default UserDTO;