import { useEffect, useState } from "react";
import { ActivationMailRequest } from "../models/dto/ActivationMailRequest";
import { useNavigate } from "react-router-dom";
import { ENV } from "../config/env";
import { useI18n } from "../i18n/i18nContext";
import { Alert, Box, CircularProgress } from "@mui/material";

export default function ActivationPage() {
  const params = new URLSearchParams(window.location.search);
  const token = params.get("token");
  const navigate = useNavigate();
  const { translation } = useI18n();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (!token) {
      setError(translation.activation.fail);
      return;
    }
    const activationRequest = new ActivationMailRequest(token);
    setLoading(true);

    fetch(`${ENV.API_BASE_URL}/users/activate`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(activationRequest),
    })
      .then((res) => {
        if (!res.ok) {
          console.log(res);
          const err = new Error(translation.activation.fail);
          err.cause = res.status;
          setLoading(false);
          setSuccess(false);
          throw err;
        } else {
          setSuccess(true);
        }
      })
      .then(() => {
        setLoading(false);
        setTimeout(() => {
          setSuccess(false);
          navigate("/login", { replace: true });
        }, 3000);
      })
      .catch((err: any) => {
        console.log(err);

        if (err.cause === 404) {
          setError(translation.activation.token_not_found);
        } else {
          setError(err.message);
        }
        setLoading(false);
      });
  }, [token, navigate]);

  return (
    <Box>
      {error && <Alert severity="error">{error}</Alert>}
      {loading && <CircularProgress />}
      {success && (
        <Alert severity="success">{translation.activation.success}</Alert>
      )}
    </Box>
  );
}
