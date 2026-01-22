import { Alert, Box, Button, Container, Stack, TextField, Typography } from "@mui/material";
import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";
import { useI18n } from "../i18n/i18nContext";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import LoginRequest from "../models/dto/LoginRequest";

export default function LoginPage() {
  const {login} = useAuth();
  const { translation } = useI18n();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string|null>(null);
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setError(null);
    setLoading(true);

    try {
      const apiEndpoint = `${ENV.API_BASE_URL}/auth/login`;
      const response = await fetch(apiEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(new LoginRequest(email, password)),
      });

      if(!response.ok) {
        const message = (await response.text()) || translation.login.invalid_credentials;
        throw new Error(message);
      }

      const data = await response.json();
      login(data.token);
      
      navigate("/", {replace: true});
    } catch (err) {
      setError(err instanceof Error ? err.message : translation.login.login_failed);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Box sx={{
        flex: 1,
        display: "flex"
      }}>
        <Container maxWidth="sm"
        sx={{
          alignContent: "center"
        }}>
          <Stack spacing={3}>
            <Typography variant="h4" textAlign="center">
              {translation.login.login}
            </Typography>

            {error && <Alert severity="error">{error}</Alert>}

            <TextField label={translation.login.email} value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
            <TextField label={translation.login.password} type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />

            <Button variant="contained" size="large" onClick={handleLogin} disabled={loading}>
              {translation.login.login}
            </Button>
          </Stack>
        </Container>
      </Box>
  );
}
