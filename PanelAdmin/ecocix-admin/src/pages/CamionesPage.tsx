import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, Card, CardContent, CircularProgress, Alert,
  Chip, Button, Avatar, alpha,
} from '@mui/material';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { adminService } from '../services/api';
import type { Camion } from '../types';
import PageHeader from '../components/PageHeader';

export default function CamionesPage() {
  const queryClient = useQueryClient();

  const { data: camiones = [], isLoading, error } = useQuery({
    queryKey: ['camiones'],
    queryFn: adminService.getCamiones,
  });

  const toggleMutation = useMutation({
    mutationFn: ({ idCamion, activo }: { idCamion: string; activo: boolean }) =>
      adminService.toggleCamion(idCamion, activo),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['camiones'] }),
  });

  if (isLoading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">Error al cargar camiones</Alert>;

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>Camiones</Typography>
        <Typography variant="body2" sx={{ color: '#404944', mt: 0.5, fontFamily: 'Inter' }}>
          Gestiona el estado y ubicación de la flota de recolección.
        </Typography>
      </Box>

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(auto-fit, minmax(280px, 1fr))' }, gap: 2 }}>
        {camiones.map((c: Camion) => (
          <Card key={c.idCamion} sx={{
            opacity: c.activo ? 1 : 0.6, transition: 'all 0.2s',
            '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' },
          }}>
            <CardContent sx={{ p: '1.5rem !important' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                <Avatar sx={{
                  bgcolor: alpha('#003527', 0.1), color: '#003527', width: 48, height: 48,
                }}>
                  <LocalShippingIcon />
                </Avatar>
                <Chip label={c.activo ? 'Activo' : 'Inactivo'} color={c.activo ? 'success' : 'error'} size="small"
                  sx={{ fontWeight: 600, fontSize: 11 }} />
              </Box>
              <Typography sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter', fontSize: 16 }}>{c.idCamion}</Typography>
              <Typography sx={{ fontSize: 13, color: '#707974', fontFamily: 'Inter', mt: 0.5 }}>Placa: {c.placa}</Typography>
              {c.coordenadas && (
                <Typography sx={{ fontFamily: 'monospace', fontSize: 12, color: '#707974', mt: 1, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <LocationOnIcon sx={{ fontSize: 14 }} />
                  {c.coordenadas.latitud.toFixed(4)}, {c.coordenadas.longitud.toFixed(4)}
                </Typography>
              )}
              <Typography sx={{ fontSize: 11, color: '#bfc9c3', mt: 1, fontFamily: 'Inter' }}>
                Última actualización: {new Date(c.ultimaActualizacion).toLocaleTimeString()}
              </Typography>
              <Button
                fullWidth variant="contained" size="small" sx={{ mt: 2, fontWeight: 600, fontSize: 12 }}
                color={c.activo ? 'error' : 'success'}
                disabled={toggleMutation.isPending}
                onClick={() => toggleMutation.mutate({ idCamion: c.idCamion, activo: !c.activo })}
              >
                {c.activo ? 'Desactivar' : 'Activar'}
              </Button>
            </CardContent>
          </Card>
        ))}
      </Box>
    </Box>
    </Box>
  );
}
