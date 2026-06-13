import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';

import { AuthProvider, useAuth } from './context/AuthContext';
import AdminLayout from './layouts/AdminLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import CamionesPage from './pages/CamionesPage';
import ReportesPage from './pages/ReportesPage';
import CiudadanosPage from './pages/CiudadanosPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AlertasPage from './pages/AlertasPage';
import LiveMapPage from './pages/LiveMapPage';

const theme = createTheme({
  cssVariables: true,
  palette: {
    primary: { main: '#1C1B1F', light: '#36343B', dark: '#121014', contrastText: '#FFFFFF' },
    secondary: { main: '#48464C', light: '#605D64', dark: '#312F35', contrastText: '#FFFFFF' },
    background: { default: '#FAFAFA', paper: '#FFFFFF' },
    grey: { 50: '#FAFAFA', 100: '#F5F5F5', 200: '#EEEEEE', 300: '#E0E0E0', 400: '#BDBDBD', 800: '#424242' },
    success: { main: '#2E7D32' },
    warning: { main: '#ED6C02' },
    info: { main: '#1976D2' },
    error: { main: '#D32F2F' },
  },
  typography: {
    fontFamily: '"Inter", "Plus Jakarta Sans", sans-serif',
    h5: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    h6: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    subtitle1: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 600 },
    button: { fontFamily: '"Inter", sans-serif', fontWeight: 600 },
  },
  shape: { borderRadius: 12 },
  shadows: [
    'none',
    '0 1px 3px rgba(0,0,0,0.04)',
    '0 2px 8px rgba(0,0,0,0.06)',
    '0 4px 16px rgba(0,0,0,0.08)',
    '0 6px 24px rgba(0,0,0,0.10)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
    '0 8px 32px rgba(0,0,0,0.12)',
  ],
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: 'var(--mui-palette-background-default)' },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 12px rgba(0,0,0,0.06)',
          border: '1px solid var(--mui-palette-grey-200)',
          borderRadius: 16,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { borderRadius: 12 },
        elevation0: { boxShadow: 'none' },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none' as const,
          fontWeight: 600,
          borderRadius: 10,
          boxShadow: 'none',
          '&:hover': { boxShadow: '0 2px 8px rgba(0,0,0,0.12)' },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          borderRadius: 8,
          border: '1px solid var(--mui-palette-grey-200)',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            backgroundColor: 'var(--mui-palette-grey-50)',
            fontWeight: 700,
            textTransform: 'uppercase' as const,
            fontSize: 11,
            letterSpacing: '0.05em',
            color: 'var(--mui-palette-text-secondary)',
            borderBottom: '1px solid var(--mui-palette-grey-200)',
          },
        },
      },
    },
    MuiTableBody: {
      styleOverrides: {
        root: {
          '& .MuiTableRow-root:hover': {
            backgroundColor: 'var(--mui-palette-grey-50)',
          },
          '& .MuiTableCell-root': {
            borderBottom: '1px solid var(--mui-palette-grey-100)',
          },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          transition: 'background-color 0.2s ease',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 10,
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          borderRadius: 10,
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 16,
          boxShadow: '0 12px 48px rgba(0,0,0,0.15)',
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 10,
        },
      },
    },
    MuiToggleButtonGroup: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          border: '1px solid var(--mui-palette-grey-200)',
          overflow: 'hidden',
        },
      },
    },
    MuiToggleButton: {
      styleOverrides: {
        root: {
          textTransform: 'none' as const,
          fontWeight: 600,
          borderRadius: 0,
          '&.Mui-selected': {
            backgroundColor: 'var(--mui-palette-primary-main)',
            color: 'var(--mui-palette-primary-contrastText)',
          },
        },
      },
    },
    MuiSlider: {
      styleOverrides: {
        root: {
          color: 'var(--mui-palette-primary-main)',
        },
      },
    },
  },
});

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/"
              element={
                <PrivateRoute>
                  <AdminLayout />
                </PrivateRoute>
              }
            >
              <Route index element={<DashboardPage />} />
              <Route path="camiones" element={<CamionesPage />} />
              <Route path="mapa" element={<LiveMapPage />} />
              <Route path="reportes" element={<ReportesPage />} />
              <Route path="ciudadanos" element={<CiudadanosPage />} />
              <Route path="admins" element={<AdminUsersPage />} />
              <Route path="alertas" element={<AlertasPage />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
