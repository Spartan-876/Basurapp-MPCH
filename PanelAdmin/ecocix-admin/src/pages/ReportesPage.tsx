import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, CircularProgress, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, Select, MenuItem, FormControl,
} from '@mui/material';
import { adminService } from '../services/api';
import type { Reporte } from '../types';

const ESTADOS = ['PENDIENTE', 'EN_PROCESO', 'RESUELTO'] as const;

const estadoColor = (e: string): 'warning' | 'info' | 'success' => {
  if (e === 'PENDIENTE') return 'warning';
  if (e === 'EN_PROCESO') return 'info';
  return 'success';
};

export default function ReportesPage() {
  const queryClient = useQueryClient();

  const { data: reportes = [], isLoading, error } = useQuery({
    queryKey: ['reportes'],
    queryFn: adminService.getReportes,
  });

  const mutation = useMutation({
    mutationFn: ({ id, estado }: { id: number; estado: string }) =>
      adminService.cambiarEstadoReporte(id, estado),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['reportes'] }),
  });

  if (isLoading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error">Error al cargar reportes</Alert>;
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>Reportes ({reportes.length})</Typography>
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
            {reportes.map((r: Reporte) => (
              <TableRow key={r.id} hover>
                <TableCell>#{r.id}</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>{r.usuarioNombre}</TableCell>
                <TableCell sx={{ maxWidth: 250, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {r.descripcion}
                </TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 12, color: 'text.secondary' }}>
                  {r.latitud?.toFixed(4)}, {r.longitud?.toFixed(4)}
                </TableCell>
                <TableCell>{new Date(r.fecha).toLocaleDateString()}</TableCell>
                <TableCell>
                  <Chip label={r.estado} color={estadoColor(r.estado)} size="small" />
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
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
