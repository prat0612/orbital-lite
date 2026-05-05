import { useEffect, useMemo, useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material';

const emptyForm = {
  name: '',
  email: '',
  role: '',
  department: '',
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function EmployeeFormDialog({ employee, open, onClose, onSubmit, loading }) {
  const [form, setForm] = useState(emptyForm);
  const [touched, setTouched] = useState({});

  useEffect(() => {
    if (open) {
      setForm(employee ? {
        name: employee.name || '',
        email: employee.email || '',
        role: employee.role || '',
        department: employee.department || '',
      } : emptyForm);
      setTouched({});
    }
  }, [employee, open]);

  const errors = useMemo(() => {
    const nextErrors = {};
    if (!form.name.trim()) {
      nextErrors.name = 'Name is required';
    }
    if (!form.email.trim()) {
      nextErrors.email = 'Email is required';
    } else if (!emailPattern.test(form.email.trim())) {
      nextErrors.email = 'Enter a valid email';
    }
    return nextErrors;
  }, [form]);

  const hasErrors = Object.keys(errors).length > 0;

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleBlur = (event) => {
    const { name } = event.target;
    setTouched((current) => ({ ...current, [name]: true }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    setTouched({ name: true, email: true });

    if (!hasErrors) {
      onSubmit({
        name: form.name.trim(),
        email: form.email.trim(),
        role: form.role.trim(),
        department: form.department.trim(),
      });
    }
  };

  return (
    <Dialog open={open} onClose={loading ? undefined : onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>{employee ? 'Edit employee' : 'Add employee'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ pt: 1 }}>
            <TextField
              autoFocus
              required
              fullWidth
              label="Name"
              name="name"
              value={form.name}
              onBlur={handleBlur}
              onChange={handleChange}
              error={Boolean(touched.name && errors.name)}
              helperText={touched.name && errors.name ? errors.name : ' '}
            />
            <TextField
              required
              fullWidth
              label="Email"
              name="email"
              type="email"
              value={form.email}
              onBlur={handleBlur}
              onChange={handleChange}
              error={Boolean(touched.email && errors.email)}
              helperText={touched.email && errors.email ? errors.email : ' '}
            />
            <TextField
              fullWidth
              label="Role"
              name="role"
              value={form.role}
              onChange={handleChange}
            />
            <TextField
              fullWidth
              label="Department"
              name="department"
              value={form.department}
              onChange={handleChange}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {employee ? 'Save changes' : 'Create employee'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
