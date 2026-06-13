import { useQuery } from '@tanstack/react-query';
import { Box, Typography, Card, CardContent, CircularProgress, Alert, Avatar, useTheme, alpha } from '@mui/material';
import DescriptionIcon from '@mui/icons-material/Description';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PeopleIcon from '@mui/icons-material/People';
import { adminService } from '../services/api';

const statsConfig = [
  { key: 'total', label: 'Reportes totales', icon: <DescriptionIcon />, colorKey: 'primary' as const },
  { key: 'pendientes', label: 'Pendientes', icon: <PendingActionsIcon />, colorKey: 'warning' as const },
  { key: 'enProceso', label: 'En proceso', icon: <AutorenewIcon />, colorKey: 'info' as const },
  { key: 'resueltos', label: 'Resueltos', icon: <CheckCircleIcon />, colorKey: 'success' as const },
  { key: 'ciudadanos', label: 'Ciudadanos registrados', icon: <PeopleIcon />, colorKey: 'secondary' as const },
];

export default function DashboardPage() {
  const theme = useTheme();
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
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(auto-fit, minmax(200px, 1fr))' }, gap: 2.5 }}>
        {statsConfig.map((s) => {
          const color = theme.palette[s.colorKey].main;
          return (
            <Card key={s.key} sx={{ transition: 'transform 0.2s, box-shadow 0.2s', '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 6px 24px rgba(0,0,0,0.1)' } }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar
                  sx={{
                    bgcolor: alpha(color, 0.1),
                    color: color,
                    width: 52,
                    height: 52,
                  }}
                >
                  {s.icon}
                </Avatar>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700, lineHeight: 1.1 }}>{values[s.key]}</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>{s.label}</Typography>
                </Box>
              </CardContent>
            </Card>
          );
        })}
      </Box>
    </Box>
  );
}
