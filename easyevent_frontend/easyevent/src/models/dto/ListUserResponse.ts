export class ListUserResponse {
    id: number;
    name: string;
    email: string;
    profilePicture: string | null;
    active: boolean;
    banned: boolean;
    roles: string[];

    constructor(id: number, name: string, email: string, profilePicture: string|null, active: boolean, banned: boolean, roles: string[]) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
        this.active = active;
        this.banned = banned;
        this.roles = roles;
    }
}

export default ListUserResponse;