import {
  Button,
  Container,
  Stack,
  Typography,
  Alert,
} from "@mui/material";
import { useSearchParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { ENV } from "../config/env";
//import { useI18n } from "../i18n/i18nContext";

export default function RecoveryPage() {
  const [searchParams] = useSearchParams();
  const email = searchParams.get("email");
  //const { translation } = useI18n();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRecover = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${ENV.API_BASE_URL}/users/recover?email=${email}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (response.ok) {
        // Recovery successful! Send them back to login
        navigate("/login", { state: { recovered: true } });
      } else {
        setError("Failed to recover account.");
      }
    } catch (err) {
      setError("Server unreachable.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 15 }}>
      <Stack spacing={3} alignItems="center">
        <Typography variant="h4">Recover Your Account</Typography>
        <Typography textAlign="center">
          The account for <strong>{email}</strong> is scheduled for deletion.
          Would you like to restore it and keep your data?
        </Typography>
        {error && <Alert severity="error">{error}</Alert>}
        <Button
          variant="contained"
          size="large"
          onClick={handleRecover}
          disabled={loading || !email}
        >
          {loading ? "Restoring..." : "Yes, Restore My Account"}
        </Button>
        <Button variant="text" onClick={() => navigate("/login")}>
          Cancel
        </Button>
      </Stack>
    </Container>
  );
}
