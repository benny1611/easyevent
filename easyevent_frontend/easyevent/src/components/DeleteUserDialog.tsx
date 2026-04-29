import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from "@mui/material";
import { useEffect, useState } from "react";

interface DeleteUserDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  userName?: string;
  loading: boolean;
}

const DeleteUserDialog = ({ open, onClose, onConfirm, userName, loading }: DeleteUserDialogProps) => {
  const [reason, setReason] = useState("");

  // Reset reason when dialog opens/closes
  useEffect(() => {
    if (!open) setReason("");
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Delete User</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Are you sure you want to delete <strong>{userName}</strong>? 
          This is a soft delete; you have 30 days to revert this action.
        </DialogContentText>
        <TextField
          autoFocus
          margin="dense"
          label="Reason for deletion"
          fullWidth
          variant="outlined"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          error={reason.length > 0 && !reason.trim()}
          helperText="You must provide a reason to delete this user."
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        <Button
          onClick={() => onConfirm(reason)}
          color="error"
          variant="contained"
          disabled={!reason.trim() || loading}
        >
          Delete
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeleteUserDialog;