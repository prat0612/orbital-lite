import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';

export default function ConfirmDeleteDialog({ employee, open, onCancel, onConfirm, loading }) {
  return (
    <Dialog open={open} onClose={loading ? undefined : onCancel} maxWidth="xs" fullWidth>
      <DialogTitle>Delete employee</DialogTitle>
      <DialogContent>
        <DialogContentText>
          Delete {employee?.name || 'this employee'} from the directory?
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button onClick={onConfirm} color="error" variant="contained" disabled={loading}>
          Delete
        </Button>
      </DialogActions>
    </Dialog>
  );
}
