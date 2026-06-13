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

const theme = createTheme({
  palette: {
    primary: { main: '#0d631b', light: '#2e7d32', dark: '#094d14' },
    secondary: { main: '#2e7d32' },
    background: { default: '#f5f5f5', paper: '#ffffff' },
  },
  typography: {
    fontFamily: '"Inter", "Plus Jakarta Sans", "Roboto", sans-serif',
  },
  shape: { borderRadius: 8 },
  components: {
    MuiButton: { styleOverrides: { root: { textTransform: 'none', fontWeight: 600 } } },
    MuiCard: { styleOverrides: { root: { boxShadow: '0 1px 3px rgba(0,0,0,0.08)', border: '1px solid #e0e0e0' } } },
    MuiChip: { styleOverrides: { root: { fontWeight: 600 } } },
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
