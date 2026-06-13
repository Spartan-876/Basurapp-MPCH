import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Box, Typography, Card, CardContent, Chip, Avatar, IconButton, useTheme, alpha,
  CircularProgress,
} from '@mui/material';
import MyLocationIcon from '@mui/icons-material/MyLocation';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import RefreshIcon from '@mui/icons-material/Refresh';
import { adminService } from '../services/api';
import type { Camion } from '../types';

const CHICLAYO_CENTER = { lat: -6.7714, lng: -79.8409 };

export default function LiveMapPage() {
  const theme = useTheme();
  const [positions, setPositions] = useState<Record<string, { lat: number; lng: number }>>({});

  const { data: camiones = [], isLoading, refetch } = useQuery({
    queryKey: ['camiones'],
    queryFn: adminService.getCamiones,
    refetchInterval: 8000,
  });

  useEffect(() => {
    const newPos: Record<string, { lat: number; lng: number }> = {};
    camiones.forEach((c: Camion) => {
      if (c.coordenadas) {
        newPos[c.idCamion] = { lat: c.coordenadas.latitud, lng: c.coordenadas.longitud };
      }
    });
    setPositions(newPos);
  }, [camiones]);

  const activeCamiones = camiones.filter((c: Camion) => c.activo);

  return (
    <Box sx={{ display: 'flex', gap: 2, height: 'calc(100vh - 128px)', minHeight: 500 }}>
      {/* Mapa */}
      <Card
        sx={{
          flex: 1,
          position: 'relative',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {/* Mapa de fondo */}
        <Box
          sx={{
            flex: 1,
            position: 'relative',
            bgcolor: 'grey.100',
            overflow: 'hidden',
          }}
        >
          {/* Grid de fondo */}
          <svg
            style={{ position: 'absolute', inset: 0, width: '100%', height: '100%' }}
            preserveAspectRatio="none"
            viewBox="0 0 100 100"
          >
            <defs>
              <pattern id="grid" width="5" height="5" patternUnits="userSpaceOnUse">
                <path d="M 5 0 L 0 0 0 5" fill="none" stroke={theme.palette.grey[300]} strokeWidth="0.15" />
              </pattern>
            </defs>
            <rect width="100" height="100" fill="url(#grid)" />
            {/* Polígono Chiclayo placeholder */}
            <polygon
              fill={alpha(theme.palette.primary.main, 0.05)}
              stroke={alpha(theme.palette.primary.main, 0.3)}
              strokeWidth="0.4"
              strokeDasharray="1,0.5"
              points="20,25 80,15 90,55 55,85 10,70"
            />
          </svg>

          {/* Marcadores de camiones */}
          {camiones.map((c: Camion) => {
            const pos = positions[c.idCamion];
            if (!pos) return null;
            const offsetX = ((pos.lng - CHICLAYO_CENTER.lng) / 0.05) * 100;
            const offsetY = ((CHICLAYO_CENTER.lat - pos.lat) / 0.05) * 100;
            const clampedX = Math.max(10, Math.min(90, 50 + offsetX * 0.4));
            const clampedY = Math.max(10, Math.min(90, 50 + offsetY * 0.4));

            return (
              <Box
                key={c.idCamion}
                sx={{
                  position: 'absolute',
                  left: `${clampedX}%`,
                  top: `${clampedY}%`,
                  transform: 'translate(-50%, -50%)',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  zIndex: 2,
                }}
              >
                <Avatar
                  sx={{
                    width: 40,
                    height: 40,
                    bgcolor: c.activo ? 'background.paper' : 'grey.300',
                    color: c.activo ? 'primary.main' : 'grey.600',
                    border: `2px solid ${c.activo ? theme.palette.primary.main : theme.palette.grey[400]}`,
                    boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
                  }}
                >
                  <LocalShippingIcon />
                </Avatar>
                <Typography
                  variant="caption"
                  sx={{
                    mt: 0.5,
                    bgcolor: 'background.paper',
                    px: 1,
                    py: 0.25,
                    borderRadius: 1,
                    fontSize: 10,
                    fontWeight: 700,
                    boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {c.placa}
                </Typography>
              </Box>
            );
          })}

          {/* Floating controls */}
          <Box sx={{ position: 'absolute', top: 12, right: 12, display: 'flex', flexDirection: 'column', gap: 1, zIndex: 3 }}>
            <IconButton
              onClick={() => refetch()}
              sx={{
                bgcolor: 'background.paper',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                '&:hover': { bgcolor: 'grey.50' },
              }}
            >
              <RefreshIcon />
            </IconButton>
            <IconButton
              sx={{
                bgcolor: 'background.paper',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                '&:hover': { bgcolor: 'grey.50' },
              }}
            >
              <MyLocationIcon />
            </IconButton>
          </Box>

          {/* Info bar inferior */}
          <Box
            sx={{
              position: 'absolute',
              bottom: 12,
              left: 12,
              right: 12,
              bgcolor: alpha(theme.palette.background.paper, 0.92),
              backdropFilter: 'blur(8px)',
              borderRadius: 2,
              px: 2.5,
              py: 1.5,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              boxShadow: '0 4px 16px rgba(0,0,0,0.1)',
              zIndex: 3,
            }}
          >
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                Flota activa
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {activeCamiones.length} de {camiones.length} camiones en servicio
              </Typography>
            </Box>
            <Chip
              label="Actualización cada 8s"
              size="small"
              color="primary"
              variant="outlined"
              sx={{ fontWeight: 600 }}
            />
          </Box>
        </Box>
      </Card>

      {/* Panel lateral */}
      <Card
        sx={{
          width: { xs: '100%', md: 340 },
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          flexShrink: 0,
        }}
      >
        <CardContent sx={{ pb: 1 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 0.5 }}>
            Estado de la Flota
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Posiciones en tiempo real
          </Typography>
        </CardContent>

        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress size={32} />
          </Box>
        ) : (
          <Box sx={{ flex: 1, overflow: 'auto', px: 2, pb: 2 }}>
            {camiones.map((c: Camion) => {
              const pos = positions[c.idCamion];
              return (
                <Card
                  key={c.idCamion}
                  sx={{
                    mb: 1.5,
                    opacity: c.activo ? 1 : 0.55,
                    border: `1px solid ${c.activo ? theme.palette.grey[200] : theme.palette.grey[100]}`,
                    boxShadow: 'none',
                    transition: 'opacity 0.2s',
                  }}
                >
                  <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Avatar
                          sx={{
                            width: 36,
                            height: 36,
                            bgcolor: c.activo ? alpha(theme.palette.primary.main, 0.1) : 'grey.100',
                            color: c.activo ? 'primary.main' : 'grey.500',
                          }}
                        >
                          <LocalShippingIcon fontSize="small" />
                        </Avatar>
                        <Box>
                          <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.2 }}>
                            {c.idCamion}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {c.placa}
                          </Typography>
                        </Box>
                      </Box>
                      <Chip
                        label={c.activo ? 'Activo' : 'Inactivo'}
                        color={c.activo ? 'success' : 'error'}
                        size="small"
                        sx={{ fontWeight: 600 }}
                      />
                    </Box>
                    {pos && (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary' }}>
                        <LocationOnIcon sx={{ fontSize: 14 }} />
                        <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                          {pos.lat.toFixed(4)}, {pos.lng.toFixed(4)}
                        </Typography>
                      </Box>
                    )}
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                      Última actualización: {new Date(c.ultimaActualizacion).toLocaleTimeString()}
                    </Typography>
                  </CardContent>
                </Card>
              );
            })}
          </Box>
        )}
      </Card>
    </Box>
  );
}
