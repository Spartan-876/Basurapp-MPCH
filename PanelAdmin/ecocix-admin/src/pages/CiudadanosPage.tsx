import { useQuery } from '@tanstack/react-query';
import {
  Box, Typography, CircularProgress, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow,
} from '@mui/material';
import { adminService } from '../services/api';

export default function CiudadanosPage() {
  const { data: ciudadanos = [], isLoading, error } = useQuery({
    queryKey: ['ciudadanos'],
    queryFn: adminService.getCiudadanos,
  });

  if (isLoading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error">Error al cargar ciudadanos</Alert>;
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>
        Ciudadanos registrados ({ciudadanos.length})
      </Typography>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Dirección</TableCell>
              <TableCell>Registro</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {ciudadanos.map((c) => (
              <TableRow key={c.id} hover>
                <TableCell sx={{ fontWeight: 600 }}>{c.nombre}</TableCell>
                <TableCell>{c.email}</TableCell>
                <TableCell>{c.direccion || '—'}</TableCell>
                <TableCell>{c.fechaRegistro ? new Date(c.fechaRegistro).toLocaleDateString() : '—'}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
