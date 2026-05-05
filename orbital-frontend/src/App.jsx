import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  InputAdornment,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import ClearIcon from '@mui/icons-material/Clear';
import DirectoryIcon from '@mui/icons-material/Badge';
import SearchIcon from '@mui/icons-material/Search';
import IconButton from '@mui/material/IconButton';
import ConfirmDeleteDialog from './components/ConfirmDeleteDialog.jsx';
import EmployeeFormDialog from './components/EmployeeFormDialog.jsx';
import EmployeeTable from './components/EmployeeTable.jsx';
import {
  createEmployee,
  deleteEmployee,
  getEmployees,
  searchEmployees,
  updateEmployee,
} from './api/employeeApi.js';

const defaultPage = {
  content: [],
  number: 0,
  size: 10,
  totalElements: 0,
};

const getErrorMessage = (error) => (
  error?.response?.data?.message
  || error?.message
  || 'Something went wrong'
);

export default function App() {
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
  const [snackbar, setSnackbar] = useState(null);

  const isSearching = searchTerm.trim().length > 0;

  const displayedEmployees = useMemo(
    () => {
      if (!isSearching) {
        return employeesPage.content;
      }

      const start = searchPage * employeesPage.size;
      return searchResults.slice(start, start + employeesPage.size);
    },
    [employeesPage.content, employeesPage.size, isSearching, searchPage, searchResults],
  );

  const loadEmployees = useCallback(async (page = employeesPage.number, size = employeesPage.size) => {
    setLoading(true);
    try {
      const data = await getEmployees({ page, size });
      setEmployeesPage(data);
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setLoading(false);
    }
  }, [employeesPage.number, employeesPage.size]);

  const runSearch = useCallback(async (query) => {
    const trimmed = query.trim();
    if (!trimmed) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    try {
      const data = await searchEmployees(trimmed);
      setSearchResults(data);
      setSearchPage(0);
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadEmployees(0, defaultPage.size);
  }, []);

  useEffect(() => {
    const handle = window.setTimeout(() => {
      runSearch(searchTerm);
    }, 250);

    return () => window.clearTimeout(handle);
  }, [runSearch, searchTerm]);

  const handleOpenCreate = () => {
    setSelectedEmployee(null);
    setFormOpen(true);
  };

  const handleOpenEdit = (employee) => {
    setSelectedEmployee(employee);
    setFormOpen(true);
  };

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
      if (isSearching) {
        await runSearch(searchTerm);
      }
      await loadEmployees(isSearching ? 0 : employeesPage.number, employeesPage.size);
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!employeeToDelete) {
      return;
    }

    setDeleting(true);
    try {
      await deleteEmployee(employeeToDelete.id);
      setSnackbar({ severity: 'success', message: 'Employee deleted' });
      setEmployeeToDelete(null);
      if (isSearching) {
        await runSearch(searchTerm);
      }
      await loadEmployees(employeesPage.number, employeesPage.size);
    } catch (error) {
      setSnackbar({ severity: 'error', message: getErrorMessage(error) });
    } finally {
      setDeleting(false);
    }
  };

  const handlePageChange = (_event, nextPage) => {
    if (isSearching) {
      setSearchPage(nextPage);
    } else {
      loadEmployees(nextPage, employeesPage.size);
    }
  };

  const handleRowsPerPageChange = (event) => {
    const nextSize = Number(event.target.value);
    if (isSearching) {
      setSearchPage(0);
      setEmployeesPage((current) => ({ ...current, size: nextSize }));
    } else {
      loadEmployees(0, nextSize);
    }
  };

  const clearSearch = () => {
    setSearchTerm('');
    setSearchResults([]);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Box
        component="header"
        sx={{
          bgcolor: 'primary.main',
          color: 'primary.contrastText',
          borderBottom: '1px solid rgba(255,255,255,0.18)',
        }}
      >
        <Container maxWidth="lg">
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            alignItems={{ xs: 'flex-start', sm: 'center' }}
            justifyContent="space-between"
            sx={{ py: 3 }}
          >
            <Stack direction="row" spacing={1.5} alignItems="center">
              <DirectoryIcon />
              <Box>
                <Typography variant="h4" component="h1">
                  Orbital Lite
                </Typography>
                <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.82)' }}>
                  Employee Directory
                </Typography>
              </Box>
            </Stack>
            <Button
              variant="contained"
              color="secondary"
              startIcon={<AddIcon />}
              onClick={handleOpenCreate}
              sx={{ bgcolor: 'secondary.main', '&:hover': { bgcolor: '#5d4037' } }}
            >
              Add Employee
            </Button>
          </Stack>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Stack spacing={2.5}>
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={2}
            justifyContent="space-between"
            alignItems={{ xs: 'stretch', md: 'center' }}
          >
            <Box>
              <Typography variant="h6" component="h2">
                Directory
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {isSearching
                  ? `${searchResults.length} search result${searchResults.length === 1 ? '' : 's'}`
                  : `${employeesPage.totalElements} employee${employeesPage.totalElements === 1 ? '' : 's'}`}
              </Typography>
            </Box>
            <TextField
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search employees"
              size="small"
              sx={{ width: { xs: '100%', md: 360 } }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
                endAdornment: searchTerm ? (
                  <InputAdornment position="end">
                    <IconButton aria-label="Clear search" onClick={clearSearch} edge="end" size="small">
                      <ClearIcon fontSize="small" />
                    </IconButton>
                  </InputAdornment>
                ) : null,
              }}
            />
          </Stack>

          <Box sx={{ position: 'relative' }}>
            <EmployeeTable
              employees={displayedEmployees}
              loading={loading}
              page={isSearching ? searchPage : employeesPage.number}
              rowsPerPage={employeesPage.size}
              totalElements={isSearching ? searchResults.length : employeesPage.totalElements}
              onPageChange={handlePageChange}
              onRowsPerPageChange={handleRowsPerPageChange}
              onEdit={handleOpenEdit}
              onDelete={setEmployeeToDelete}
            />
            {loading && displayedEmployees.length > 0 && (
              <CircularProgress
                size={28}
                sx={{
                  position: 'absolute',
                  right: 20,
                  top: 18,
                }}
              />
            )}
          </Box>
        </Stack>
      </Container>

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
