import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, CircularProgress, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, Select, MenuItem, FormControl,
  InputLabel, TextField, Card, CardContent,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import FilterListIcon from '@mui/icons-material/FilterList';
import { adminService } from '../services/api';
import type { Reporte } from '../types';
import PageHeader from '../components/PageHeader';

const ESTADOS = ['PENDIENTE', 'EN_PROCESO', 'RESUELTO'] as const;

const estadoColor = (e: string): 'warning' | 'info' | 'success' => {
  if (e === 'PENDIENTE') return 'warning';
  if (e === 'EN_PROCESO') return 'info';
  return 'success';
};

export default function ReportesPage() {
  const queryClient = useQueryClient();
  const [fechaDesde, setFechaDesde] = useState('');
  const [fechaHasta, setFechaHasta] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('');

  const { data: reportes = [], isLoading, error } = useQuery({
    queryKey: ['reportes'],
    queryFn: adminService.getReportes,
  });

  const mutation = useMutation({
    mutationFn: ({ id, estado }: { id: number; estado: string }) =>
      adminService.cambiarEstadoReporte(id, estado),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['reportes'] }),
  });

  const filteredReportes = reportes.filter((r: Reporte) => {
    if (estadoFilter && r.estado !== estadoFilter) return false;
    if (fechaDesde && r.fecha.split('T')[0] < fechaDesde) return false;
    if (fechaHasta && r.fecha.split('T')[0] > fechaHasta) return false;
    return true;
  });

  if (isLoading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">Error al cargar reportes</Alert>;

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>
          Reportes Ciudadanos
        </Typography>
        <Typography variant="body2" sx={{ color: '#404944', mt: 0.5, fontFamily: 'Inter' }}>
          Gestiona y da seguimiento a los reportes enviados por los ciudadanos.
        </Typography>
      </Box>

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ p: '1.5rem !important' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <FilterListIcon sx={{ color: '#404944', fontSize: 20 }} />
            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#151c27', fontFamily: 'Inter' }}>Filtros</Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <TextField
              label="Desde" type="date" size="small" value={fechaDesde}
              onChange={(e) => setFechaDesde(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              sx={{ minWidth: 160 }}
            />
            <TextField
              label="Hasta" type="date" size="small" value={fechaHasta}
              onChange={(e) => setFechaHasta(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              sx={{ minWidth: 160 }}
            />
            <FormControl size="small" sx={{ minWidth: 180 }}>
              <InputLabel>Estado</InputLabel>
              <Select value={estadoFilter} label="Estado" onChange={(e) => setEstadoFilter(e.target.value)}>
                <MenuItem value="">Todos</MenuItem>
                {ESTADOS.map((e) => (
                  <MenuItem key={e} value={e}>{e}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </CardContent>
      </Card>

      {/* Stats Cards */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' }, gap: 2, mb: 4 }}>
        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Total</Typography>
            <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#151c27', fontFamily: 'Inter' }}>
              {filteredReportes.length}
            </Typography>
          </CardContent>
        </Card>
        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Pendientes</Typography>
            <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#ED6C02', fontFamily: 'Inter' }}>
              {filteredReportes.filter((r) => r.estado === 'PENDIENTE').length}
            </Typography>
          </CardContent>
        </Card>
        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="body2" sx={{ color: '#404944', fontFamily: 'Inter' }}>Resueltos</Typography>
            <Typography sx={{ fontWeight: 700, fontSize: 28, color: '#006c49', fontFamily: 'Inter' }}>
              {filteredReportes.filter((r) => r.estado === 'RESUELTO').length}
            </Typography>
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
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, height: 100, alignItems: 'flex-end' }}>
            {['L', 'M', 'M', 'J', 'V', 'S', 'D'].map((d, i) => {
              const heights = [60, 75, 50, 85, 70, 35, 25];
              return (
                <Box key={d} sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.5 }}>
                  <Box sx={{
                    width: '100%', borderRadius: 1, bgcolor: i === 3 ? '#003527' : alpha('#003527', 0.2),
                    height: heights[i],
                  }} />
                  <Typography sx={{ fontSize: 11, color: '#707974', fontWeight: 500, fontFamily: 'Inter' }}>{d}</Typography>
                </Box>
              );
            })}
          </Box>
        </CardContent>
      </Card>

      {/* Tabla de Reportes */}
      <Card>
        <CardContent sx={{ p: '1.5rem !important' }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', mb: 2, fontFamily: 'Inter' }}>
            Reportes ({filteredReportes.length})
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Ciudadano</TableCell>
                  <TableCell>Descripción</TableCell>
                  <TableCell>Ubicación</TableCell>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell>Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredReportes.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} sx={{ textAlign: 'center', py: 4, color: '#707974' }}>
                      No hay reportes que mostrar
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredReportes.map((r: Reporte) => (
                    <TableRow key={r.id} hover>
                      <TableCell sx={{ fontWeight: 600, color: '#003527', fontSize: 13 }}>#{r.id}</TableCell>
                      <TableCell sx={{ fontWeight: 500, fontSize: 13 }}>{r.usuarioNombre}</TableCell>
                      <TableCell sx={{ maxWidth: 250, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontSize: 13 }}>
                        {r.descripcion}
                      </TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: 12, color: '#707974' }}>
                        {r.latitud?.toFixed(4)}, {r.longitud?.toFixed(4)}
                      </TableCell>
                      <TableCell sx={{ fontSize: 13 }}>{new Date(r.fecha).toLocaleDateString()}</TableCell>
                      <TableCell>
                        <Chip label={r.estado} color={estadoColor(r.estado)} size="small" sx={{ fontWeight: 600, fontSize: 11 }} />
                      </TableCell>
                      <TableCell>
                        <FormControl size="small" sx={{ minWidth: 130 }}>
                          <Select
                            value={r.estado}
                            onChange={(e) => mutation.mutate({ id: r.id, estado: e.target.value })}
                            size="small"
                          >
                            {ESTADOS.map((e) => (
                              <MenuItem key={e} value={e}>{e}</MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
    </Box>
  );
}


