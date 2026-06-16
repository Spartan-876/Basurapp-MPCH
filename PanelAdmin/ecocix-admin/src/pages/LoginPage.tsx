import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, CircularProgress, Avatar, useTheme, alpha,
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import ecocixLogo from '../assets/ecocix-logo.png';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Credenciales incorrectas');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: `linear-gradient(135deg, ${theme.palette.grey[100]}, ${theme.palette.background.default})`,
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 420, mx: 2 }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Avatar
              sx={{
                width: 72,
                height: 72,
                mx: 'auto',
                mb: 2,
                backgroundColor: 'transparent' ,
              }}
            >
              <Box component="img" src={ecocixLogo} sx={{ width: 72, height: 72, borderRadius: '50%' }} />
            </Avatar>
            <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main' }}>
              EcoCix Admin
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Panel de administración
            </Typography>
          </Box>

          <form onSubmit={handleSubmit}>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <TextField
              fullWidth label="Correo" type="email" margin="normal"
              value={email} onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@ecocix.com" required
            />
            <TextField
              fullWidth label="Contraseña" type="password" margin="normal"
              value={password} onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••" required
              slotProps={{ htmlInput: { minLength: 6 } }}
            />
            <Button
              fullWidth variant="contained" type="submit" size="large"
              disabled={loading} sx={{ mt: 3, py: 1.5 }}
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <LockOutlinedIcon />}
            >
              {loading ? 'Ingresando...' : 'Iniciar Sesión'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
