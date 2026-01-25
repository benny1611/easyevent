import { Alert, Avatar, Box, Button, IconButton, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import CreateUserRequest from "../models/dto/CreateUserRequest";
import { ENV } from "../config/env";
import { PhotoCamera } from "@mui/icons-material";
import { useI18n } from "../i18n/i18nContext";

interface RegistrationFormState {
    name: string;
    email: string;
    password: string;
    repeatPassword: string;
    profilePicture?: File;
}

const RegisterPage: React.FC = () => {
    const { translation } = useI18n();

    const [form, setForm] = useState<RegistrationFormState>({
        name: "",
        email: "",
        password: "",
        repeatPassword: "",
    });
    
    const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleChange = (field: keyof RegistrationFormState) => (event: React.ChangeEvent<HTMLInputElement>) => {
        setForm((prev) => ({
            ...prev,
            [field]: event.target.value,
        }));
    };

    const handleProfilePictureChange = (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        const file = event.target.files?.[0];
        if(!file) return;
        setForm((prev) => ({
            ...prev,
            profilePicture: file,
        }));

        setAvatarPreview(URL.createObjectURL(file));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if(form.password !== form.repeatPassword) {
            setError(translation.register.pws_don_t_match);
            return;
        }

        setLoading(true);

        try {
            const dto = new CreateUserRequest(
                form.name,
                form.email,
                form.password,
                ["ROLE_USER"]
            );

            const formData = new FormData();
            formData.append("name", dto.name);
            formData.append("email", dto.email);
            formData.append("password", dto.password);

            dto.roles.forEach((role) => {
                formData.append("roles", role);
            });

            if (form.profilePicture) {
                formData.append("profilePicture", form.profilePicture);
            }
            
            const apiEndpoint = `${ENV.API_BASE_URL}/users/create`;

            const response = await fetch(apiEndpoint, {
                method: "POST",
                body: formData,
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || translation.register.registration_failed);
            }

            alert("YAAAY"); // TODO: Change to redirect to home
        } catch (err: any) {
            setError(err.message ?? "Something went wrong");
        } finally {
            setLoading(false);
        }
    };


    return (
        <Box
            component="form"
            onSubmit={handleSubmit}
            maxWidth={600}
            width="100%"
            mx="auto"
            mt={8}
            px={4}
            py={4}
            display="flex"
            flexDirection="column"
            gap={2}
        >
            <Typography variant="h5" textAlign="center" fontWeight={600} sx={{ mb: 3 }}>
                {translation.register.create_account}
            </Typography>

            {error && <Alert severity="error">{error}</Alert>}

            <Box display="flex" justifyContent="center">
                <Stack>
                    <Avatar
                    src={avatarPreview ?? undefined}
                    sx={{ width: 90, height: 90 }}
                    />
                    <IconButton 
                        color="primary"
                        component="label">
                            <input 
                                hidden
                                accept="image/*"
                                type="file"
                                onChange={handleProfilePictureChange} />
                            <PhotoCamera />
                    </IconButton>
                </Stack>
                
            </Box>

            <TextField
                label={translation.register.name}
                required
                value={form.name}
                fullWidth
                onChange={handleChange("name")}/>
            
            <TextField
                label={translation.register.email}
                type="email"
                required
                value={form.email}
                fullWidth
                onChange={handleChange("email")} />

            <TextField
                label={translation.register.password}
                type="password"
                required
                value={form.password}
                fullWidth
                onChange={handleChange("password")} />
            
            <TextField
                label={translation.register.repeat_password}
                type="password"
                required
                value={form.repeatPassword}
                fullWidth
                onChange={handleChange("repeatPassword")} />

            <Button
                fullWidth
                type="submit"
                variant="contained"
                size="large"
                sx={{
                textTransform: 'none',
                fontWeight: 600,
                fontSize: 20
                }}
                disabled={loading}>
                    {loading ? translation.register.creating_account : translation.register.register}
            </Button>
        </Box>
    );

}

export default RegisterPage;