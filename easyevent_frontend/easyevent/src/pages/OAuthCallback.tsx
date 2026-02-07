import { useEffect } from "react";
import { ENV } from "../config/env";
import LoginResponse from "../models/dto/LoginResponse";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";

const OAuthCallback = () => {
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

    return (<div>Signing you in ...</div>);
}

export default OAuthCallback;