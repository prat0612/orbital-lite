import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Container,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Snackbar,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DashboardIcon from '@mui/icons-material/Dashboard';
import LogoutIcon from '@mui/icons-material/Logout';
import PeopleIcon from '@mui/icons-material/People';
import WorkIcon from '@mui/icons-material/Work';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import AssessmentIcon from '@mui/icons-material/Assessment';
import ConfirmDeleteDialog from './components/ConfirmDeleteDialog.jsx';
import EmployeeFormDialog from './components/EmployeeFormDialog.jsx';
import EmployeeTable from './components/EmployeeTable.jsx';
import { login } from './api/authApi.js';
import {
  createEmployee,
  deleteEmployee,
  getEmployees,
  searchEmployees,
  updateEmployee,
} from './api/employeeApi.js';
import {
  applyLeave,
  approveLeave,
  getAllLeaves,
  getMyLeaves,
  rejectLeave,
} from './api/leaveApi.js';
import {
  createUser,
  deleteUser,
  getUsers,
  updateUser,
} from './api/userApi.js';
import {
  getDashboardSummary,
  getEmployeeReport,
  getLeaveReport,
  getNotifications,
} from './api/dashboardApi.js';

const defaultPage = {
  content: [],
  number: 0,
  size: 10,
  totalElements: 0,
};

const today = new Date().toISOString().slice(0, 10);

const getErrorMessage = (error) => (
  error?.response?.data?.message
  || error?.message
  || 'Something went wrong'
);

const hasRole = (user, roles) => roles.some((role) => user?.roles?.includes(role));

const storedUser = () => {
  const raw = localStorage.getItem('orbital_user');
  return raw ? JSON.parse(raw) : null;
};

export default function App() {
  const [user, setUser] = useState(storedUser);
  const [activeView, setActiveView] = useState('dashboard');
  const [snackbar, setSnackbar] = useState(null);

  const isAdmin = hasRole(user, ['ADMIN']);
  const isManager = hasRole(user, ['ADMIN', 'MANAGER']);

  const views = useMemo(() => {
    return [
      { value: 'dashboard', label: 'Dashboard', icon: <DashboardIcon fontSize="small" />, visible: isManager },
      { value: 'employees', label: 'Employees', icon: <WorkIcon fontSize="small" />, visible: true },
      { value: 'leaves', label: 'Leaves', icon: <EventAvailableIcon fontSize="small" />, visible: true },
      { value: 'users', label: 'Users', icon: <PeopleIcon fontSize="small" />, visible: isAdmin },
      { value: 'reports', label: 'Reports', icon: <AssessmentIcon fontSize="small" />, visible: isManager },
    ].filter((view) => view.visible);
  }, [isAdmin, isManager]);

  useEffect(() => {
    if (!views.some((view) => view.value === activeView)) {
      setActiveView(views[0]?.value || 'employees');
    }
  }, [activeView, views]);

  const handleLogin = (authResponse) => {
    localStorage.setItem('orbital_token', authResponse.token);
    localStorage.setItem('orbital_user', JSON.stringify({
      username: authResponse.username,
      roles: authResponse.roles,
    }));
    setUser({ username: authResponse.username, roles: authResponse.roles });
    setActiveView(authResponse.roles.includes('EMPLOYEE') ? 'employees' : 'dashboard');
  };

  const handleLogout = () => {
    localStorage.removeItem('orbital_token');
    localStorage.removeItem('orbital_user');
    setUser(null);
  };

  if (!user) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Box component="header" sx={{ bgcolor: 'primary.main', color: 'primary.contrastText' }}>
        <Container maxWidth="lg">
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={2}
            alignItems={{ xs: 'flex-start', md: 'center' }}
            justifyContent="space-between"
            sx={{ py: 2.5 }}
          >
            <Box>
              <Typography variant="h4" component="h1">Orbital Lite</Typography>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 0.75, flexWrap: 'wrap' }}>
                <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.82)' }}>{user.username}</Typography>
                {user.roles.map((role) => (
                  <Chip key={role} label={role} size="small" sx={{ bgcolor: 'rgba(255,255,255,0.16)', color: 'white' }} />
                ))}
              </Stack>
            </Box>
            <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
              Logout
            </Button>
          </Stack>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 3 }}>
        <Paper variant="outlined" sx={{ mb: 3 }}>
          <Tabs
            value={activeView}
            onChange={(_event, value) => setActiveView(value)}
            variant="scrollable"
            scrollButtons="auto"
          >
            {views.map((view) => (
              <Tab key={view.value} value={view.value} icon={view.icon} iconPosition="start" label={view.label} />
            ))}
          </Tabs>
        </Paper>

        {activeView === 'dashboard' && <DashboardView setSnackbar={setSnackbar} />}
        {activeView === 'employees' && <EmployeesView canManage={isManager} canDelete={isAdmin} setSnackbar={setSnackbar} />}
        {activeView === 'leaves' && <LeavesView canApprove={isManager} setSnackbar={setSnackbar} />}
        {activeView === 'users' && <UsersView setSnackbar={setSnackbar} />}
        {activeView === 'reports' && <ReportsView setSnackbar={setSnackbar} />}
      </Container>

      <Snackbar
        open={Boolean(snackbar)}
        autoHideDuration={4000}
        onClose={() => setSnackbar(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        {snackbar ? (
          <Alert severity={snackbar.severity} variant="filled" onClose={() => setSnackbar(null)}>
            {snackbar.message}
          </Alert>
        ) : null}
      </Snackbar>
    </Box>
  );
}

function LoginPage({ onLogin }) {
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      onLogin(await login(form));
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', display: 'grid', placeItems: 'center', px: 2 }}>
      <Paper variant="outlined" sx={{ width: '100%', maxWidth: 420, p: 3 }}>
        <Typography variant="h4" component="h1" sx={{ mb: 0.5 }}>Orbital Lite</Typography>
        <Typography color="text.secondary" sx={{ mb: 3 }}>Workforce Management</Typography>
        <Stack component="form" spacing={2} onSubmit={handleSubmit}>
          <TextField
            label="Username"
            value={form.username}
            onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))}
            required
            fullWidth
          />
          <TextField
            label="Password"
            type="password"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            required
            fullWidth
          />
          {error && <Alert severity="error">{error}</Alert>}
          <Button type="submit" variant="contained" disabled={loading}>
            Login
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
}

function DashboardView({ setSnackbar }) {
  const [summary, setSummary] = useState(null);
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    Promise.all([getDashboardSummary(), getNotifications()])
      .then(([summaryData, notificationData]) => {
        setSummary(summaryData);
        setNotifications(notificationData);
      })
      .catch((error) => setSnackbar({ severity: 'error', message: getErrorMessage(error) }));
  }, [setSnackbar]);

  if (!summary) {
    return <Typography color="text.secondary">Loading dashboard...</Typography>;
  }

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
        <Metric label="Employees" value={summary.totalEmployees} />
        <Metric label="Users" value={summary.totalUsers} />
        <Metric label="Pending Leaves" value={summary.pendingLeaves} />
      </Stack>
      <DataSection title="Recent Audit Logs">
        <SimpleTable
          columns={['User', 'Action', 'Entity', 'Entity ID']}
          rows={summary.recentAuditLogs.map((log) => [log.user, log.action, log.entity, log.entityId || '-'])}
        />
      </DataSection>
      <DataSection title="Notifications">
        <SimpleTable
          columns={['Message', 'Created']}
          rows={notifications.slice(0, 5).map((notification) => [notification.message, formatDate(notification.createdAt)])}
        />
      </DataSection>
    </Stack>
  );
}

function EmployeesView({ canManage, canDelete, setSnackbar }) {
  const [employeesPage, setEmployeesPage] = useState(defaultPage);
  const [searchResults, setSearchResults] = useState([]);
  const [searchPage, setSearchPage] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [employeeToDelete, setEmployeeToDelete] = useState(null);
  const isSearching = searchTerm.trim().length > 0;

  const displayedEmployees = useMemo(() => {
    if (!isSearching) {
      return employeesPage.content;
    }
    const start = searchPage * employeesPage.size;
    return searchResults.slice(start, start + employeesPage.size);
  }, [employeesPage, isSearching, searchPage, searchResults]);

  const loadEmployees = useCallback(async (page = employeesPage.number, size = employeesPage.size) => {
    setLoading(true);
    try {
      setEmployeesPage(await getEmployees({ page, size }));
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setLoading(false);
    }
  }, [employeesPage.number, employeesPage.size, setSnackbar]);

  useEffect(() => {
    loadEmployees(0, defaultPage.size);
  }, []);

  useEffect(() => {
    const handle = window.setTimeout(async () => {
      const query = searchTerm.trim();
      if (!query) {
        setSearchResults([]);
        return;
      }
      setLoading(true);
      try {
        setSearchResults(await searchEmployees(query));
        setSearchPage(0);
      } catch (error) {
        setSnackbar({ severity: 'error', message: getErrorMessage(error) });
      } finally {
        setLoading(false);
      }
    }, 250);
    return () => window.clearTimeout(handle);
  }, [searchTerm, setSnackbar]);

  const handleSubmit = async (payload) => {
    setSaving(true);
    try {
      if (selectedEmployee) {
        await updateEmployee(selectedEmployee.id, payload);
        setSnackbar({ severity: 'success', message: 'Employee updated' });
      } else {
        await createEmployee(payload);
        setSnackbar({ severity: 'success', message: 'Employee created' });
      }
      setFormOpen(false);
      setSelectedEmployee(null);
      await loadEmployees(0, employeesPage.size);
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteEmployee(employeeToDelete.id);
      setSnackbar({ severity: 'success', message: 'Employee deleted' });
      setEmployeeToDelete(null);
      await loadEmployees(employeesPage.number, employeesPage.size);
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setDeleting(false);
    }
  };

  return (
    <Stack spacing={2.5}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} justifyContent="space-between">
        <Box>
          <Typography variant="h6">Employees</Typography>
          <Typography color="text.secondary" variant="body2">
            {isSearching ? `${searchResults.length} search results` : `${employeesPage.totalElements} employees`}
          </Typography>
        </Box>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <TextField
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search employees"
            size="small"
          />
          {canManage && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                setSelectedEmployee(null);
                setFormOpen(true);
              }}
            >
              Add Employee
            </Button>
          )}
        </Stack>
      </Stack>
      <EmployeeTable
        employees={displayedEmployees}
        loading={loading}
        page={isSearching ? searchPage : employeesPage.number}
        rowsPerPage={employeesPage.size}
        totalElements={isSearching ? searchResults.length : employeesPage.totalElements}
        onPageChange={(_event, nextPage) => (isSearching ? setSearchPage(nextPage) : loadEmployees(nextPage, employeesPage.size))}
        onRowsPerPageChange={(event) => loadEmployees(0, Number(event.target.value))}
        onEdit={(employee) => {
          if (canManage) {
            setSelectedEmployee(employee);
            setFormOpen(true);
          }
        }}
        onDelete={(employee) => {
          if (canDelete) {
            setEmployeeToDelete(employee);
          }
        }}
      />
      <EmployeeFormDialog
        employee={selectedEmployee}
        open={formOpen}
        loading={saving}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmit}
      />
      <ConfirmDeleteDialog
        employee={employeeToDelete}
        open={Boolean(employeeToDelete)}
        loading={deleting}
        onCancel={() => setEmployeeToDelete(null)}
        onConfirm={handleDelete}
      />
    </Stack>
  );
}

function LeavesView({ canApprove, setSnackbar }) {
  const [myLeaves, setMyLeaves] = useState([]);
  const [allLeaves, setAllLeaves] = useState([]);
  const [form, setForm] = useState({ startDate: today, endDate: today, reason: '' });

  const loadLeaves = useCallback(async () => {
    try {
      setMyLeaves(await getMyLeaves());
      if (canApprove) {
        setAllLeaves(await getAllLeaves());
      }
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  }, [canApprove, setSnackbar]);

  useEffect(() => {
    loadLeaves();
  }, [loadLeaves]);

  const submitLeave = async (event) => {
    event.preventDefault();
    try {
      await applyLeave(form);
      setSnackbar({ severity: 'success', message: 'Leave request submitted' });
      setForm({ startDate: today, endDate: today, reason: '' });
      await loadLeaves();
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  };

  const decide = async (id, action) => {
    try {
      await (action === 'approve' ? approveLeave(id) : rejectLeave(id));
      setSnackbar({ severity: 'success', message: `Leave ${action}d` });
      await loadLeaves();
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  };

  return (
    <Stack spacing={3}>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>Apply Leave</Typography>
        <Stack component="form" onSubmit={submitLeave} spacing={2} direction={{ xs: 'column', md: 'row' }}>
          <TextField
            label="Start"
            type="date"
            value={form.startDate}
            onChange={(event) => setForm((current) => ({ ...current, startDate: event.target.value }))}
            InputLabelProps={{ shrink: true }}
            required
          />
          <TextField
            label="End"
            type="date"
            value={form.endDate}
            onChange={(event) => setForm((current) => ({ ...current, endDate: event.target.value }))}
            InputLabelProps={{ shrink: true }}
            required
          />
          <TextField
            label="Reason"
            value={form.reason}
            onChange={(event) => setForm((current) => ({ ...current, reason: event.target.value }))}
            required
            fullWidth
          />
          <Button type="submit" variant="contained">Submit</Button>
        </Stack>
      </Paper>
      <LeaveTable title="My Leaves" leaves={myLeaves} />
      {canApprove && <LeaveTable title="All Leaves" leaves={allLeaves} onDecision={decide} />}
    </Stack>
  );
}

function UsersView({ setSnackbar }) {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({ username: '', password: '', enabled: true, roles: 'EMPLOYEE' });

  const loadUsers = useCallback(async () => {
    try {
      setUsers(await getUsers());
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  }, [setSnackbar]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const submitUser = async (event) => {
    event.preventDefault();
    try {
      await createUser({ ...form, roles: [form.roles] });
      setSnackbar({ severity: 'success', message: 'User created' });
      setForm({ username: '', password: '', enabled: true, roles: 'EMPLOYEE' });
      await loadUsers();
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  };

  const toggleEnabled = async (account) => {
    try {
      await updateUser(account.id, {
        username: account.username,
        enabled: !account.enabled,
        roles: account.roles,
      });
      await loadUsers();
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  };

  const removeUser = async (account) => {
    try {
      await deleteUser(account.id);
      await loadUsers();
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    }
  };

  return (
    <Stack spacing={3}>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>Create User</Typography>
        <Stack component="form" onSubmit={submitUser} spacing={2} direction={{ xs: 'column', md: 'row' }}>
          <TextField label="Username" value={form.username} onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))} required />
          <TextField label="Password" type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} required />
          <FormControl sx={{ minWidth: 160 }}>
            <InputLabel>Role</InputLabel>
            <Select label="Role" value={form.roles} onChange={(event) => setForm((current) => ({ ...current, roles: event.target.value }))}>
              <MenuItem value="ADMIN">ADMIN</MenuItem>
              <MenuItem value="MANAGER">MANAGER</MenuItem>
              <MenuItem value="EMPLOYEE">EMPLOYEE</MenuItem>
            </Select>
          </FormControl>
          <Button type="submit" variant="contained">Create</Button>
        </Stack>
      </Paper>
      <DataSection title="Users">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Username</TableCell>
                <TableCell>Roles</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((account) => (
                <TableRow key={account.id}>
                  <TableCell>{account.username}</TableCell>
                  <TableCell>{account.roles.join(', ')}</TableCell>
                  <TableCell>{account.enabled ? 'Enabled' : 'Disabled'}</TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => toggleEnabled(account)}>
                      {account.enabled ? 'Disable' : 'Enable'}
                    </Button>
                    <Button size="small" color="error" onClick={() => removeUser(account)}>Delete</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </DataSection>
    </Stack>
  );
}

function ReportsView({ setSnackbar }) {
  const [leaveReport, setLeaveReport] = useState(null);
  const [employeeReport, setEmployeeReport] = useState(null);

  useEffect(() => {
    Promise.all([getLeaveReport(), getEmployeeReport()])
      .then(([leaves, employees]) => {
        setLeaveReport(leaves);
        setEmployeeReport(employees);
      })
      .catch((error) => setSnackbar({ severity: 'error', message: getErrorMessage(error) }));
  }, [setSnackbar]);

  if (!leaveReport || !employeeReport) {
    return <Typography color="text.secondary">Loading reports...</Typography>;
  }

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
        <Metric label="Pending" value={leaveReport.pending} />
        <Metric label="Approved" value={leaveReport.approved} />
        <Metric label="Rejected" value={leaveReport.rejected} />
        <Metric label="Employees" value={employeeReport.totalEmployees} />
      </Stack>
      <DataSection title="Employees by Department">
        <SimpleTable columns={['Department', 'Total']} rows={Object.entries(employeeReport.byDepartment)} />
      </DataSection>
      <DataSection title="Employees by Role">
        <SimpleTable columns={['Role', 'Total']} rows={Object.entries(employeeReport.byRole)} />
      </DataSection>
    </Stack>
  );
}

function LeaveTable({ title, leaves, onDecision }) {
  return (
    <DataSection title={title}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>User</TableCell>
              <TableCell>Dates</TableCell>
              <TableCell>Reason</TableCell>
              <TableCell>Status</TableCell>
              {onDecision && <TableCell align="right">Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {leaves.map((leave) => (
              <TableRow key={leave.id}>
                <TableCell>{leave.username}</TableCell>
                <TableCell>{leave.startDate} to {leave.endDate}</TableCell>
                <TableCell>{leave.reason}</TableCell>
                <TableCell><Chip label={leave.status} size="small" /></TableCell>
                {onDecision && (
                  <TableCell align="right">
                    <Button size="small" disabled={leave.status !== 'PENDING'} onClick={() => onDecision(leave.id, 'approve')}>Approve</Button>
                    <Button size="small" color="error" disabled={leave.status !== 'PENDING'} onClick={() => onDecision(leave.id, 'reject')}>Reject</Button>
                  </TableCell>
                )}
              </TableRow>
            ))}
            {leaves.length === 0 && (
              <TableRow>
                <TableCell colSpan={onDecision ? 5 : 4}>
                  <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>No leave requests</Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </DataSection>
  );
}

function Metric({ label, value }) {
  return (
    <Paper variant="outlined" sx={{ p: 2, flex: 1 }}>
      <Typography color="text.secondary" variant="body2">{label}</Typography>
      <Typography variant="h4">{value}</Typography>
    </Paper>
  );
}

function DataSection({ title, children }) {
  return (
    <Paper variant="outlined" sx={{ overflow: 'hidden' }}>
      <Box sx={{ p: 2 }}>
        <Typography variant="h6">{title}</Typography>
      </Box>
      <Divider />
      {children}
    </Paper>
  );
}

function SimpleTable({ columns, rows }) {
  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            {columns.map((column) => <TableCell key={column}>{column}</TableCell>)}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row, index) => (
            <TableRow key={`${row.join('-')}-${index}`}>
              {row.map((cell, cellIndex) => <TableCell key={cellIndex}>{cell}</TableCell>)}
            </TableRow>
          ))}
          {rows.length === 0 && (
            <TableRow>
              <TableCell colSpan={columns.length}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>No data</Typography>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  }).format(new Date(value));
}
