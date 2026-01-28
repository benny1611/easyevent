import { Alert, Box, Button, Container, Link, Stack, TextField, Typography } from "@mui/material";
import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";
import { useI18n } from "../i18n/i18nContext";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import LoginRequest from "../models/dto/LoginRequest";
import { Link as RouterLink } from "react-router-dom";
import LoginResponse from "../models/dto/LoginResponse";

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
      const input = new LoginResponse(data.token);
      login(input);
      
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
          alignContent: "center",
          mt: 5
        }}>
          <Stack spacing={3}>
            <Typography variant="h4" textAlign="center">
              {translation.login.login}
            </Typography>

            {error && <Alert severity="error">{error}</Alert>}

            <TextField label={translation.login.email} value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
            <TextField 
            label={translation.login.password} 
            type="password" 
            value={password} onChange={(e) => setPassword(e.target.value)} 
            onKeyDown={(e) => {
              if(e.key === 'Enter') {
                handleLogin();
              }
            }}
            autoComplete="current-password" />

            <Button variant="contained" 
            size="large" 
            onClick={handleLogin} 
            disabled={loading}
            fullWidth
            sx={{
              textTransform: 'none',
              fontWeight: 600,
              fontSize: 20
            }}>
              {translation.login.login}
            </Button>
            <Typography variant="subtitle2" textAlign="left">
              {translation.login.no_account} <Link component={RouterLink} to="/register">{translation.login.sign_in}</Link>
            </Typography>
          </Stack>
        </Container>
      </Box>
  );
}
