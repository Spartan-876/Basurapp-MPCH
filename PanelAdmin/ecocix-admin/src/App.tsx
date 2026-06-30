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
import ConfigPage from './pages/ConfigPage';

const theme = createTheme({
  cssVariables: true,
  palette: {
    primary: { main: '#003527', light: '#006c49', dark: '#002117', contrastText: '#FFFFFF' },
    secondary: { main: '#006c49', light: '#4edea3', dark: '#005236', contrastText: '#FFFFFF' },
    background: { default: '#f9f9ff', paper: '#FFFFFF' },
    grey: { 50: '#f9f9ff', 100: '#f0f3ff', 200: '#e7eefe', 300: '#dce2f3', 400: '#bfc9c3', 500: '#707974', 800: '#404944' },
    success: { main: '#006c49' },
    warning: { main: '#ED6C02' },
    info: { main: '#1976D2' },
    error: { main: '#ba1a1a' },
    text: { primary: '#151c27', secondary: '#404944' },
    divider: '#bfc9c3',
  },
  typography: {
    fontFamily: '"Inter", sans-serif',
    h4: { fontWeight: 700, fontSize: '32px', letterSpacing: '-0.02em', lineHeight: 1.2 },
    h5: { fontWeight: 600, fontSize: '24px', letterSpacing: '-0.01em', lineHeight: 1.3 },
    h6: { fontWeight: 600, fontSize: '20px', letterSpacing: '-0.01em', lineHeight: 1.4 },
    subtitle1: { fontWeight: 600, fontSize: '16px' },
    subtitle2: { fontWeight: 500, fontSize: '14px' },
    body1: { fontWeight: 400, fontSize: '16px', lineHeight: 1.6 },
    body2: { fontWeight: 400, fontSize: '14px', lineHeight: 1.5 },
    caption: { fontWeight: 500, fontSize: '12px', letterSpacing: '0.02em', lineHeight: 1 },
    button: { fontWeight: 600, letterSpacing: '0.01em' },
  },
  shape: { borderRadius: 8 },
  shadows: [
    'none',
    '0 1px 3px rgba(0,0,0,0.04)',
    '0 2px 6px -1px rgba(0,0,0,0.06)',
    '0 4px 6px -1px rgba(0,0,0,0.1)',
    '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
    '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
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
          boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
          borderRadius: 8,
          backgroundColor: '#FFFFFF',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { borderRadius: 8 },
        elevation0: { boxShadow: 'none' },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none' as const,
          fontWeight: 600,
          borderRadius: 8,
          boxShadow: 'none',
          '&:hover': { boxShadow: '0 2px 6px -1px rgba(0,0,0,0.1)' },
        },
        contained: {
          backgroundColor: '#003527',
          '&:hover': { backgroundColor: '#002117' },
        },
        outlined: {
          borderColor: '#bfc9c3',
          '&:hover': { borderColor: '#003527', backgroundColor: 'rgba(0,53,39,0.04)' },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          borderRadius: 9999,
          fontSize: 12,
        },
        filled: {
          backgroundColor: 'rgba(0, 108, 73, 0.1)',
          color: '#003527',
          border: '1px solid rgba(0, 108, 73, 0.2)',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            backgroundColor: '#f9f9ff',
            fontWeight: 600,
            fontSize: 12,
            letterSpacing: '0.02em',
            color: '#404944',
            borderBottom: '1px solid #e7eefe',
            textTransform: 'uppercase' as const,
          },
        },
      },
    },
    MuiTableBody: {
      styleOverrides: {
        root: {
          '& .MuiTableRow-root:hover': {
            backgroundColor: '#f0f3ff',
          },
          '& .MuiTableCell-root': {
            borderBottom: '1px solid #f0f3ff',
            fontSize: 14,
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
            borderRadius: 8,
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 12,
          boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)',
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 8 },
      },
    },
    MuiToggleButtonGroup: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          border: '1px solid #e7eefe',
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
            backgroundColor: '#003527',
            color: '#FFFFFF',
          },
        },
      },
    },
    MuiSlider: {
      styleOverrides: {
        root: { color: '#003527' },
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
              <Route path="configuracion" element={<ConfigPage />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
