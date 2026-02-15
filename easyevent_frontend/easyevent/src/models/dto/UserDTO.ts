export class UserDTO {
    email: string;
    name: string;
    profilePicture: string|null;
    active: boolean;

    constructor(email: string, name: string, profilePicture: string, active: boolean) {
        this.email = email;
        this.name = name;
        this.profilePicture = profilePicture;
        this.active = active;
    }
}

export default UserDTO;