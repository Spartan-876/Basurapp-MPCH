import { useQuery } from '@tanstack/react-query';
import {
  Box, Typography, Card, CardContent, CircularProgress, Alert, Avatar,
  IconButton, alpha,
} from '@mui/material';
import AssessmentIcon from '@mui/icons-material/Assessment';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PeopleIcon from '@mui/icons-material/People';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { adminService } from '../services/api';
import PageHeader from '../components/PageHeader';

export default function DashboardPage() {
  const { data: stats, isLoading: statsLoading, error: statsError } = useQuery({
    queryKey: ['statsReportes'],
    queryFn: adminService.getStatsReportes,
  });

  const { data: ciudadanosCount, isLoading: ciuLoading } = useQuery({
    queryKey: ['ciudadanosCount'],
    queryFn: adminService.getCiudadanosCount,
  });

  const loading = statsLoading || ciuLoading;
  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  if (statsError) return <Alert severity="error">Error al cargar estadísticas</Alert>;

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>
          Panel de Control
        </Typography>
        <Typography variant="body2" sx={{ color: '#404944', mt: 0.5, fontFamily: 'Inter' }}>
          Resumen general del sistema de recolección y reportes ciudadanos.
        </Typography>
      </Box>

      {/* Stats Cards */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' }, gap: 2, mb: 4 }}>
        {/* Total Reportes */}
        <Card>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '1.5rem !important' }}>
            <Avatar sx={{ bgcolor: alpha('#006c49', 0.1), color: '#006c49', width: 48, height: 48 }}>
              <AssessmentIcon />
            </Avatar>
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Total Reportes</Typography>
              <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#151c27', fontFamily: 'Inter' }}>
                {stats?.total ?? 0}
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Pendientes */}
        <Card>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '1.5rem !important' }}>
            <Avatar sx={{ bgcolor: alpha('#ED6C02', 0.1), color: '#ED6C02', width: 48, height: 48 }}>
              <PendingActionsIcon />
            </Avatar>
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Pendientes</Typography>
              <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#151c27', fontFamily: 'Inter' }}>
                {stats?.pendientes ?? 0}
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Resueltos */}
        <Card>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '1.5rem !important' }}>
            <Avatar sx={{ bgcolor: alpha('#006c49', 0.1), color: '#006c49', width: 48, height: 48 }}>
              <CheckCircleIcon />
            </Avatar>
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Resueltos</Typography>
              <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#151c27', fontFamily: 'Inter' }}>
                {stats?.resueltos ?? 0}
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Ciudadanos Registrados */}
        <Card>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '1.5rem !important' }}>
            <Avatar sx={{ bgcolor: alpha('#1976D2', 0.1), color: '#1976D2', width: 48, height: 48 }}>
              <PeopleIcon />
            </Avatar>
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Ciudadanos</Typography>
              <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#151c27', fontFamily: 'Inter' }}>
                {ciudadanosCount ?? 0}
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Weekly Chart Placeholder */}
      <Card sx={{ mb: 4 }}>
        <CardContent sx={{ p: '1.5rem !important' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>
              Actividad Semanal
            </Typography>
            <IconButton size="small"><MoreVertIcon /></IconButton>
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, height: 120, alignItems: 'flex-end' }}>
            {['L', 'M', 'M', 'J', 'V', 'S', 'D'].map((d, i) => {
              const heights = [65, 80, 55, 90, 75, 40, 30];
              return (
                <Box key={d} sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.5 }}>
                  <Box sx={{
                    width: '100%', borderRadius: 1, bgcolor: i === 3 ? '#003527' : alpha('#003527', 0.2),
                    height: heights[i], transition: 'height 0.3s',
                  }} />
                  <Typography sx={{ fontSize: 11, color: '#707974', fontWeight: 500, fontFamily: 'Inter' }}>{d}</Typography>
                </Box>
              );
            })}
          </Box>
        </CardContent>
      </Card>

      {/* Resumen Rápido */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2 }}>
        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', mb: 2, fontFamily: 'Inter' }}>
              Distribución de Reportes
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {[
                { label: 'Pendientes', value: stats?.pendientes ?? 0, total: stats?.total ?? 1, color: '#ED6C02' },
                { label: 'En Proceso', value: stats?.enProceso ?? 0, total: stats?.total ?? 1, color: '#1976D2' },
                { label: 'Resueltos', value: stats?.resueltos ?? 0, total: stats?.total ?? 1, color: '#006c49' },
              ].map((item) => (
                <Box key={item.label}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography sx={{ fontSize: 13, color: '#404944', fontFamily: 'Inter' }}>{item.label}</Typography>
                    <Typography sx={{ fontSize: 13, fontWeight: 600, color: '#151c27', fontFamily: 'Inter' }}>{item.value}</Typography>
                  </Box>
                  <Box sx={{ height: 6, borderRadius: 3, bgcolor: '#e7eefe', overflow: 'hidden' }}>
                    <Box sx={{ height: '100%', width: `${(item.value / item.total) * 100}%`, bgcolor: item.color, borderRadius: 3, transition: 'width 0.3s' }} />
                  </Box>
                </Box>
              ))}
            </Box>
          </CardContent>
        </Card>

        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', mb: 1, fontFamily: 'Inter' }}>
              Mapa de Densidad
            </Typography>
            <Typography variant="body2" sx={{ color: '#707974', mb: 2, fontFamily: 'Inter', fontSize: 13 }}>
              Rendimiento de recolección geolocalizado
            </Typography>
            <Box sx={{
              height: 180, bgcolor: '#f0f3ff', borderRadius: 1, display: 'flex',
              alignItems: 'center', justifyContent: 'center', border: '1px dashed #bfc9c3',
            }}>
              <Typography sx={{ color: '#707974', fontSize: 13, fontFamily: 'Inter' }}>Mapa de calor próximamente</Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Box>
    </Box>
  );
}
