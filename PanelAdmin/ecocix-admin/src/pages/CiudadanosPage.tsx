import { useQuery } from '@tanstack/react-query';
import {
  Box, Typography, CircularProgress, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Avatar, Card, CardContent,
} from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import { adminService } from '../services/api';
import PageHeader from '../components/PageHeader';

export default function CiudadanosPage() {
  const { data: ciudadanos = [], isLoading, error } = useQuery({
    queryKey: ['ciudadanos'],
    queryFn: adminService.getCiudadanos,
  });

  if (isLoading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">Error al cargar ciudadanos</Alert>;

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>
          Ciudadanos registrados
        </Typography>
        <Typography variant="body2" sx={{ color: '#404944', mt: 0.5, fontFamily: 'Inter' }}>
          Total: {ciudadanos.length} ciudadanos en la plataforma.
        </Typography>
      </Box>

      <Card>
        <CardContent sx={{ p: '1.5rem !important' }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Correo</TableCell>
                  <TableCell>Dirección</TableCell>
                  <TableCell>Registro</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {ciudadanos.map((c) => (
                  <TableRow key={c.id} hover>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Avatar sx={{ width: 28, height: 28, bgcolor: '#064E3B', color: '#FFFFFF', fontSize: 11, fontWeight: 700 }}>
                          {(c.nombre || 'C').charAt(0).toUpperCase()}
                        </Avatar>
                        <Typography sx={{ fontWeight: 600, fontSize: 13, fontFamily: 'Inter' }}>{c.nombre}</Typography>
                      </Box>
                    </TableCell>
                    <TableCell sx={{ fontSize: 13 }}>{c.email}</TableCell>
                    <TableCell sx={{ fontSize: 13 }}>{c.direccion || '—'}</TableCell>
                    <TableCell sx={{ fontSize: 13 }}>{c.fechaRegistro ? new Date(c.fechaRegistro).toLocaleDateString() : '—'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
    </Box>
  );
}
