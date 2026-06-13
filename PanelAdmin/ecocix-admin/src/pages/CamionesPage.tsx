import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, Card, CardContent, CircularProgress, Alert,
  Chip, Button, Grid, Avatar, useTheme, alpha,
} from '@mui/material';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { adminService } from '../services/api';
import type { Camion } from '../types';

export default function CamionesPage() {
  const queryClient = useQueryClient();
  const theme = useTheme();

  const { data: camiones = [], isLoading, error } = useQuery({
    queryKey: ['camiones'],
    queryFn: adminService.getCamiones,
  });

  const toggleMutation = useMutation({
    mutationFn: ({ idCamion, activo }: { idCamion: string; activo: boolean }) =>
      adminService.toggleCamion(idCamion, activo),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['camiones'] }),
  });

  if (isLoading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error">Error al cargar camiones</Alert>;
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>Camiones</Typography>
      <Grid container spacing={2.5}>
        {camiones.map((c: Camion) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={c.idCamion}>
            <Card
              sx={{
                opacity: c.activo ? 1 : 0.6,
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 6px 24px rgba(0,0,0,0.1)' },
              }}
            >
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                  <Avatar
                    sx={{
                      bgcolor: alpha(theme.palette.primary.main, 0.1),
                      color: 'primary.main',
                      width: 48,
                      height: 48,
                    }}
                  >
                    <LocalShippingIcon />
                  </Avatar>
                  <Chip
                    label={c.activo ? 'Activo' : 'Inactivo'}
                    color={c.activo ? 'success' : 'error'}
                    size="small"
                  />
                </Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>{c.idCamion}</Typography>
                <Typography variant="body2" color="text.secondary">Placa: {c.placa}</Typography>
                {c.coordenadas && (
                  <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace', fontSize: 12, mt: 0.5 }}>
                    <LocationOnIcon sx={{ fontSize: 14, verticalAlign: 'middle', mr: 0.5 }} />
                    {c.coordenadas.latitud.toFixed(4)}, {c.coordenadas.longitud.toFixed(4)}
                  </Typography>
                )}
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
                  Última actualización: {new Date(c.ultimaActualizacion).toLocaleTimeString()}
                </Typography>
                <Button
                  fullWidth variant="contained" size="small"
                  color={c.activo ? 'error' : 'success'}
                  sx={{ mt: 2 }}
                  disabled={toggleMutation.isPending}
                  onClick={() => toggleMutation.mutate({ idCamion: c.idCamion, activo: !c.activo })}
                >
                  {c.activo ? 'Desactivar' : 'Activar'}
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
