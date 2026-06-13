import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, CircularProgress, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Chip, Button, TextField,
  Card, CardContent, Dialog, DialogTitle, DialogContent, DialogActions,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import { adminService } from '../services/api';
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
      setShowForm(false);
      setNombre('');
      setEmail('');
      setPassword('');
      setFormError('');
    },
    onError: (err: any) => {
      setFormError(err.response?.data?.error || 'Error al registrar admin');
    },
  });

  const desactivarMutation = useMutation({
    mutationFn: (id: number) => adminService.desactivarAdmin(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admins'] });
      setConfirmId(null);
    },
  });

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    registerMutation.mutate({ nombre, email, password });
  };

  if (isLoading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error">Error al cargar administradores</Alert>;
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Administradores ({admins.length})</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancelar' : 'Nuevo Admin'}
        </Button>
      </Box>

      {showForm && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>Crear Administrador</Typography>
            <form onSubmit={handleRegister}>
              {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-end' }}>
                <TextField label="Nombre" value={nombre} onChange={e => setNombre(e.target.value)} required size="small" sx={{ flex: 1, minWidth: 150 }} />
                <TextField label="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} required size="small" sx={{ flex: 1, minWidth: 150 }} />
                <TextField label="Contraseña" type="password" value={password} onChange={e => setPassword(e.target.value)} required size="small" slotProps={{ htmlInput: { minLength: 6 } }} sx={{ flex: 1, minWidth: 150 }} />
                <Button type="submit" variant="contained" disabled={registerMutation.isPending}>
                  {registerMutation.isPending ? 'Creando...' : 'Crear'}
                </Button>
              </Box>
            </form>
          </CardContent>
        </Card>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 12 }}>Nombre</TableCell>
              <TableCell sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 12 }}>Email</TableCell>
              <TableCell sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 12 }}>Estado</TableCell>
              <TableCell sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 12 }}>Fecha de creación</TableCell>
              <TableCell sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 12 }}>Acción</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {admins.map((a: Admin) => (
              <TableRow key={a.id} hover>
                <TableCell sx={{ fontWeight: 600 }}>{a.nombre}</TableCell>
                <TableCell>{a.email}</TableCell>
                <TableCell>
                  <Chip label={a.activo ? 'Activo' : 'Inactivo'} color={a.activo ? 'success' : 'error'} size="small" />
                </TableCell>
                <TableCell>{a.fechaCreacion ? new Date(a.fechaCreacion).toLocaleDateString() : '—'}</TableCell>
                <TableCell>
                  {a.activo && (
                    <Button color="error" size="small" startIcon={<BlockIcon />} onClick={() => setConfirmId(a.id)}>
                      Desactivar
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={confirmId !== null} onClose={() => setConfirmId(null)}>
        <DialogTitle>¿Desactivar administrador?</DialogTitle>
        <DialogContent>
          <Typography>Esta acción desactivará el acceso de este administrador al panel.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmId(null)}>Cancelar</Button>
          <Button color="error" variant="contained" disabled={desactivarMutation.isPending}
            onClick={() => confirmId !== null && desactivarMutation.mutate(confirmId)}>
            {desactivarMutation.isPending ? 'Desactivando...' : 'Desactivar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
