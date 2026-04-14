import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from "@mui/material";
import { useState } from "react";

interface BanDialogProps {
  open: boolean;
  userName?: string;
  onClose: () => void;
  onConfirm: (reason: string) => void;
}

const BanReasonDialog = ({ open, userName, onClose, onConfirm }: BanDialogProps) => {
  // Localizing the "noisy" state here prevents parent re-renders
  const [reason, setReason] = useState("");

  const handleConfirm = () => {
    if (reason.trim()) {
      onConfirm(reason);
      setReason(""); // Reset for next time
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>Ban User: {userName}</DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary', mt: 1 }}>
          Please provide a mandatory reason for banning this user.
        </Typography>
        <TextField
          autoFocus
          margin="dense"
          label="Reason for Ban"
          fullWidth
          required
          error={!reason.trim()}
          value={reason}
          onChange={(e) => setReason(e.target.value)} // Only this component re-renders now!
        />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose}>Cancel</Button>
        <Button 
          onClick={handleConfirm} 
          variant="contained" 
          color="error" 
          disabled={!reason.trim()}
        >
          Confirm Ban
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BanReasonDialog;