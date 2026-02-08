import { useEffect } from "react";
import { ENV } from "../config/env";
import LoginResponse from "../models/dto/LoginResponse";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";
import { Box, CircularProgress, Typography } from "@mui/material";
import { useI18n } from "../i18n/i18nContext";

const OAuthCallback = () => {
    const { translation } = useI18n();
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");
    const {login} = useAuth();
    const navigate = useNavigate();
    useEffect(() => {
        if (!code) {
            return;
        }

        fetch(`${ENV.BARE_URL_BASE}/api/auth/oauth/exchange`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ code }),
        })
        .then(res => res.json())
        .then(data => {
            const input = new LoginResponse(data.token);
            login(input);
            navigate("/", {replace: true});
        })
    });

    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                gap: 2,
            }}
        >
            <CircularProgress />
            <Typography color="text.secondary">
                {translation.login.signing_in}
            </Typography>
        </Box>
    );
}

export default OAuthCallback;