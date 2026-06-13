import { useQuery } from '@tanstack/react-query';
import { Box, Typography, Card, CardContent, CircularProgress, Alert } from '@mui/material';
import DescriptionIcon from '@mui/icons-material/Description';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PeopleIcon from '@mui/icons-material/People';
import { adminService } from '../services/api';

const statsConfig = [
  { key: 'total', label: 'Reportes totales', icon: <DescriptionIcon />, color: '#0d631b' },
  { key: 'pendientes', label: 'Pendientes', icon: <PendingActionsIcon />, color: '#ed6c02' },
  { key: 'enProceso', label: 'En proceso', icon: <AutorenewIcon />, color: '#1976d2' },
  { key: 'resueltos', label: 'Resueltos', icon: <CheckCircleIcon />, color: '#2e7d32' },
  { key: 'ciudadanos', label: 'Ciudadanos registrados', icon: <PeopleIcon />, color: '#7b1fa2' },
];

export default function DashboardPage() {
  const { data: stats, isLoading: statsLoading, error: statsError } = useQuery({
    queryKey: ['statsReportes'],
    queryFn: adminService.getStatsReportes,
  });

  const { data: ciudadanosCount, isLoading: ciuLoading, error: ciuError } = useQuery({
    queryKey: ['ciudadanosCount'],
    queryFn: adminService.getCiudadanosCount,
  });

  const loading = statsLoading || ciuLoading;
  const error = statsError || ciuError;

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error" sx={{ mb: 2 }}>Error al cargar estadísticas</Alert>;
  }

  const values: Record<string, number> = {
    total: stats?.total || 0,
    pendientes: stats?.pendientes || 0,
    enProceso: stats?.enProceso || 0,
    resueltos: stats?.resueltos || 0,
    ciudadanos: ciudadanosCount || 0,
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>Dashboard</Typography>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(auto-fit, minmax(200px, 1fr))' }, gap: 2 }}>
        {statsConfig.map((s) => (
          <Card key={s.key}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ color: s.color, display: 'flex', alignItems: 'center' }}>
                {s.icon}
              </Box>
              <Box>
                <Typography variant="h4" sx={{ fontWeight: 700 }}>{values[s.key]}</Typography>
                <Typography variant="body2" color="text.secondary">{s.label}</Typography>
              </Box>
            </CardContent>
          </Card>
        ))}
      </Box>
    </Box>
  );
}
