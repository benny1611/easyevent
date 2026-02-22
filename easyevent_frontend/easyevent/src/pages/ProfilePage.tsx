import {
  Avatar,
  Box,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
  type SelectChangeEvent,
} from "@mui/material";
import { useI18n } from "../i18n/i18nContext";
import { PhotoCamera } from "@mui/icons-material";
import { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";

const ProfilePage = () => {
  const { translation } = useI18n();
  const { profilePictureUrl, token } = useAuth();
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [language, setLanguage] = useState<string>('en');

  const [nameError, setNameError] = useState(false);
  const [nameTouched, setNameTouched] = useState(false);
  const [emailError, setEmailError] = useState(false);
  const [emailTouched, setEmailTouched] = useState(false);
  const [passwordError, setPasswordError] = useState(false);
  const [passwordTouched, setPasswordTouched] = useState(false);
  const [repeatPasswordError, setRepeatPasswordError] = useState(false);
  const [repeatPasswordTouched, setRepeatPasswordTouched] = useState(false);

  const handleLoad = async () => {
    const apiEndpoint = `${ENV.API_BASE_URL}/users/`;
    const response = await fetch(apiEndpoint, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || translation.register.registration_failed);
    }

    const data = await response.json();
    if (data.name) {
      setForm((prev) => ({
        ...prev,
        ["name"]: data.name,
      }));
    }
    if (data.email) {
      setForm((prev) => ({
        ...prev,
        ["email"]: data.email,
      }));
    }
    if (data.language) {
      setForm((prev) => ({
        ...prev,
        ["language"]: data.language,
      }));
    } else {
      const english = translation.languages.filter((lng) => lng.code === "en")[0];      
      setForm((prev) => ({
        ...prev,
        ["language"]: english.code,
      }));
    }
  };

  useEffect(() => {
    const loadData = async () => {
      await handleLoad();
    };

    loadData();
  }, []);

  interface ChangeProfileFormState {
    name: string;
    email: string;
    language: string;
    oldPassword: string;
    password: string;
    repeatPassword: string;
    profilePicture?: File;
  }
  const [form, setForm] = useState<ChangeProfileFormState>({
    name: "",
    email: "",
    language: "",
    oldPassword: "",
    password: "",
    repeatPassword: "",
  });

  const handleProfilePictureChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (file.size > 5242880) {
      setError(translation.register.file_too_big);
      return;
    }
    if (error === translation.register.file_too_big) {
      setError(null);
    }
    setForm((prev) => ({
      ...prev,
      profilePicture: file,
    }));

    setAvatarPreview(URL.createObjectURL(file));
  };

  const handleChangeSelect = (event: SelectChangeEvent<string>) => {
    setLanguage(event.target.value as string);
    setForm((prev) => ({
        ...prev,
        ["language"]: event.target.value as string,
      }));
  };

  const handleChange =
    (field: keyof ChangeProfileFormState) =>
    (event: React.ChangeEvent<HTMLInputElement>) => {
      switch (field) {
        case "email":
          setEmailError(!isValidEmail(event.target.value));
          break;
        case "name":
          setNameError(!isValidName(event.target.value));
          break;
        case "password":
          setPasswordError(!isValidPassword(event.target.value));
          break;
        case "repeatPassword":
          setRepeatPasswordError(!isRepeatPasswordEquals(event.target.value));
          break;
      }
      setForm((prev) => ({
        ...prev,
        [field]: event.target.value,
      }));
    };

  const handlePreview = () => {
    if (avatarPreview) {
      return avatarPreview;
    } else if (profilePictureUrl) {
      return `${ENV.BARE_URL_BASE}${profilePictureUrl}`;
    } else {
      return undefined;
    }
  };

  const isValidName = (name: string) => name.trim() !== "";

  const isValidEmail = (email: string) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const isValidPassword = (pass: string) =>
    /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(pass);

  const isRepeatPasswordEquals = (rep: string) => form.password === rep;

  return (
    <Box
      component="form"
      noValidate
      //onSubmit={handleSubmit}
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
      <Typography
        variant="h4"
        textAlign="center"
        fontWeight={600}
        sx={{ mb: 3 }}
      >
        {translation.profile.title}
      </Typography>
      <Box display="flex" justifyContent="center">
        <Stack>
          <Avatar src={handlePreview()} sx={{ width: 90, height: 90 }} />
          <IconButton color="primary" component="label">
            <input
              hidden
              accept="image/*"
              type="file"
              onChange={handleProfilePictureChange}
            />
            <PhotoCamera />
          </IconButton>
        </Stack>
      </Box>

      <TextField
        label={translation.profile.name}
        onBlur={() => {
          setNameTouched(true);
          setNameError(!isValidName(form.name));
        }}
        error={nameTouched && nameError}
        required
        value={form.name}
        fullWidth
        helperText={
          nameTouched && nameError ? translation.profile.name_required : ""
        }
        onChange={handleChange("name")}
      />

      <TextField
        label={translation.profile.email}
        onBlur={() => {
          setEmailTouched(true);
          setEmailError(!isValidEmail(form.email));
        }}
        error={emailTouched && emailError}
        helperText={
          emailTouched && emailError ? translation.profile.email_required : ""
        }
        type="email"
        required
        value={form.email}
        fullWidth
        onChange={handleChange("email")}
      />

      <FormControl variant="outlined" fullWidth>
        <InputLabel id="language-select-label">Language</InputLabel>
        <Select
          labelId="language-select-label"
          value={language}
          onChange={handleChangeSelect}
          label="Language"
        >
          {translation.languages.map((lng) => (
            <MenuItem value={lng.code}>{lng.name}</MenuItem>
          ))}
        </Select>
      </FormControl>

      <TextField
        label={translation.profile.old_password}
        onBlur={() => {
          setPasswordTouched(true);
          setPasswordError(!isValidPassword(form.password));
        }}
        error={passwordTouched && passwordError}
        helperText={
          passwordTouched && passwordError ? translation.profile.pass_form : ""
        }
        type="password"
        required
        value={form.password}
        fullWidth
        onChange={handleChange("password")}
      />

      <TextField
        label={translation.profile.new_password}
        onBlur={() => {
          setPasswordTouched(true);
          setPasswordError(!isValidPassword(form.password));
        }}
        error={passwordTouched && passwordError}
        helperText={
          passwordTouched && passwordError ? translation.profile.pass_form : ""
        }
        type="password"
        required
        value={form.password}
        fullWidth
        onChange={handleChange("password")}
      />

      <TextField
        label={translation.profile.repeat_password}
        onFocus={() => {
          setRepeatPasswordTouched(true);
          setRepeatPasswordError(!isRepeatPasswordEquals(form.repeatPassword));
        }}
        error={repeatPasswordTouched && repeatPasswordError}
        helperText={
          repeatPasswordTouched && repeatPasswordError
            ? translation.profile.repeat_pass_helper
            : ""
        }
        type="password"
        required
        value={form.repeatPassword}
        fullWidth
        onChange={handleChange("repeatPassword")}
      />
    </Box>
  );
};

export default ProfilePage;
