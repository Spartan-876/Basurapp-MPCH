import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, CircularProgress, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, Button, TextField,
  Card, CardContent, Dialog, DialogTitle, DialogContent, DialogActions, Avatar,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import { adminService } from '../services/api';
import PageHeader from '../components/PageHeader';
import type { Admin } from '../types';

export default function AdminUsersPage() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState('');
  const [confirmId, setConfirmId] = useState<number | null>(null);

  const { data: admins = [], isLoading, error } = useQuery({
    queryKey: ['admins'],
    queryFn: adminService.listarAdmins,
  });

  const registerMutation = useMutation({
    mutationFn: ({ nombre, email, password }: { nombre: string; email: string; password: string }) =>
      adminService.registrarAdmin(nombre, email, password),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admins'] });
      setShowForm(false); setNombre(''); setEmail(''); setPassword(''); setFormError('');
    },
    onError: (err: any) => setFormError(err.response?.data?.error || 'Error al registrar admin'),
  });

  const desactivarMutation = useMutation({
    mutationFn: (id: number) => adminService.desactivarAdmin(id),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admins'] }); setConfirmId(null); },
  });

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    registerMutation.mutate({ nombre, email, password });
  };

  if (isLoading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">Error al cargar administradores</Alert>;

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>
            Administradores
          </Typography>
          <Typography variant="body2" sx={{ color: '#404944', mt: 0.5, fontFamily: 'Inter' }}>
            Gestiona los usuarios con acceso al panel de administración.
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setShowForm(!showForm)}
          sx={{ bgcolor: '#003527', '&:hover': { bgcolor: '#002117' }, fontWeight: 600, fontSize: 12 }}>
          {showForm ? 'Cancelar' : 'Nuevo Admin'}
        </Button>
      </Box>

      {showForm && (
        <Card sx={{ mb: 3 }}>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', mb: 2, fontFamily: 'Inter' }}>
              Crear Administrador
            </Typography>
            <form onSubmit={handleRegister}>
              {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-end' }}>
                <TextField label="Nombre" value={nombre} onChange={e => setNombre(e.target.value)} required size="small" sx={{ flex: 1, minWidth: 150 }} />
                <TextField label="Correo" type="email" value={email} onChange={e => setEmail(e.target.value)} required size="small" sx={{ flex: 1, minWidth: 150 }} />
                <TextField label="Contraseña" type="password" value={password} onChange={e => setPassword(e.target.value)} required size="small" slotProps={{ htmlInput: { minLength: 6 } }} sx={{ flex: 1, minWidth: 150 }} />
                <Button type="submit" variant="contained" disabled={registerMutation.isPending}
                  sx={{ bgcolor: '#003527', '&:hover': { bgcolor: '#002117' }, fontWeight: 600 }}>
                  {registerMutation.isPending ? 'Creando...' : 'Crear'}
                </Button>
              </Box>
            </form>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent sx={{ p: '1.5rem !important' }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Correo</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell>Fecha de creación</TableCell>
                  <TableCell>Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {admins.map((a: Admin) => (
                  <TableRow key={a.id} hover>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Avatar sx={{ width: 28, height: 28, bgcolor: '#064E3B', color: '#FFFFFF', fontSize: 11, fontWeight: 700 }}>
                          {(a.nombre || 'A').charAt(0).toUpperCase()}
                        </Avatar>
                        <Typography sx={{ fontWeight: 600, fontSize: 13, fontFamily: 'Inter' }}>{a.nombre}</Typography>
                      </Box>
                    </TableCell>
                    <TableCell sx={{ fontSize: 13 }}>{a.email}</TableCell>
                    <TableCell>
                      <Chip label={a.activo ? 'Activo' : 'Inactivo'} color={a.activo ? 'success' : 'error'} size="small"
                        sx={{ fontWeight: 600, fontSize: 11 }} />
                    </TableCell>
                    <TableCell sx={{ fontSize: 13 }}>{a.fechaCreacion ? new Date(a.fechaCreacion).toLocaleDateString() : '—'}</TableCell>
                    <TableCell>
                      {a.activo && (
                        <Button color="error" size="small" startIcon={<BlockIcon />} onClick={() => setConfirmId(a.id)}
                          sx={{ fontWeight: 600, fontSize: 12, textTransform: 'none' }}>
                          Desactivar
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      <Dialog open={confirmId !== null} onClose={() => setConfirmId(null)}>
        <DialogTitle sx={{ fontFamily: 'Inter', fontWeight: 700 }}>¿Desactivar administrador?</DialogTitle>
        <DialogContent>
          <Typography sx={{ fontFamily: 'Inter' }}>Esta acción desactivará el acceso de este administrador al panel.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmId(null)} sx={{ fontWeight: 600 }}>Cancelar</Button>
          <Button color="error" variant="contained" disabled={desactivarMutation.isPending}
            onClick={() => confirmId !== null && desactivarMutation.mutate(confirmId)}
            sx={{ fontWeight: 600 }}>
            {desactivarMutation.isPending ? 'Desactivando...' : 'Desactivar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
    </Box>
  );
}
