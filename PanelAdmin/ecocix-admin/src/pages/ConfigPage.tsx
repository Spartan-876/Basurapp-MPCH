import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box, Typography, Card, CardContent, Button, Avatar, Tab, Tabs,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip,
  CircularProgress, alpha,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import GroupIcon from '@mui/icons-material/Group';
import { adminService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { Admin, Camion } from '../types';
import PageHeader from '../components/PageHeader';

export default function ConfigPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [tab, setTab] = useState(0);

  const { data: admins = [], isLoading: adminsLoading } = useQuery({
    queryKey: ['admins'],
    queryFn: adminService.listarAdmins,
  });

  const { data: camiones = [], isLoading: camionesLoading } = useQuery({
    queryKey: ['camiones'],
    queryFn: adminService.getCamiones,
  });

  const desactivarMutation = useMutation({
    mutationFn: (id: number) => adminService.desactivarAdmin(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admins'] }),
  });

  const toggleMutation = useMutation({
    mutationFn: ({ idCamion, activo }: { idCamion: string; activo: boolean }) =>
      adminService.toggleCamion(idCamion, activo),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['camiones'] }),
  });

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter' }}>
          Configuración
        </Typography>
        <Typography variant="body2" sx={{ color: '#404944', mt: 0.5, fontFamily: 'Inter' }}>
          Administra los parámetros globales, flota y usuarios de la plataforma EcoCix.
        </Typography>
      </Box>

      <Tabs
        value={tab} onChange={(_, v) => setTab(v)}
        sx={{
          mb: 3, '& .MuiTab-root': { textTransform: 'none', fontWeight: 600, fontSize: 13, minHeight: 44, fontFamily: 'Inter' },
          '& .MuiTabs-indicator': { bgcolor: '#003527' },
        }}
      >
        <Tab icon={<PersonIcon />} iconPosition="start" label="Perfil de Usuario" />
        <Tab icon={<LocalShippingIcon />} iconPosition="start" label="Gestión de Flota" />
        <Tab icon={<GroupIcon />} iconPosition="start" label="Usuarios del Sistema" />
      </Tabs>

      {/* Tab 0: Perfil — solo lectura */}
      {tab === 0 && (
        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter', mb: 3 }}>
              Información Personal
            </Typography>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3, p: 2, bgcolor: '#f9f9ff', borderRadius: 1 }}>
              <Avatar sx={{ width: 56, height: 56, bgcolor: '#064E3B', color: '#FFFFFF', fontSize: 20, fontWeight: 700 }}>
                {(user?.nombre || 'A').charAt(0).toUpperCase()}
              </Avatar>
              <Box>
                <Typography sx={{ fontWeight: 700, color: '#151c27', fontSize: 16, fontFamily: 'Inter' }}>
                  {user?.nombre || 'Admin'}
                </Typography>
                <Typography sx={{ fontSize: 13, color: '#707974', fontFamily: 'Inter' }}>
                  {user?.email}
                </Typography>
              </Box>
            </Box>

            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
              <Box sx={{ p: 2, bgcolor: '#f9f9ff', borderRadius: 1 }}>
                <Typography sx={{ fontSize: 11, color: '#707974', fontWeight: 500, mb: 0.5, fontFamily: 'Inter' }}>Nombre Completo</Typography>
                <Typography sx={{ fontSize: 14, fontWeight: 600, color: '#151c27', fontFamily: 'Inter' }}>{user?.nombre || '—'}</Typography>
              </Box>
              <Box sx={{ p: 2, bgcolor: '#f9f9ff', borderRadius: 1 }}>
                <Typography sx={{ fontSize: 11, color: '#707974', fontWeight: 500, mb: 0.5, fontFamily: 'Inter' }}>Correo Electrónico</Typography>
                <Typography sx={{ fontSize: 14, fontWeight: 600, color: '#151c27', fontFamily: 'Inter' }}>{user?.email || '—'}</Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Tab 1: Flota — tabla funcional */}
      {tab === 1 && (
        <Card>
          <CardContent sx={{ p: '1.5rem !important' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#151c27', fontFamily: 'Inter', mb: 3 }}>
              Listado de Vehículos
            </Typography>

            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID/Matrícula</TableCell>
                    <TableCell>Placa</TableCell>
                    <TableCell>Estado</TableCell>
                    <TableCell>Acción</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {camionesLoading ? (
                    <TableRow><TableCell colSpan={4} align="center"><CircularProgress size={24} /></TableCell></TableRow>
                  ) : camiones.length === 0 ? (
                    <TableRow><TableCell colSpan={4} align="center" sx={{ color: '#707974' }}>No hay vehículos registrados</TableCell></TableRow>
                  ) : (
                    camiones.map((c: Camion) => (
                      <TableRow key={c.idCamion} hover>
                        <TableCell sx={{ fontWeight: 600, fontFamily: 'Inter', fontSize: 13 }}>{c.idCamion}</TableCell>
                        <TableCell sx={{ fontSize: 13 }}>{c.placa}</TableCell>
                        <TableCell>
                          <Chip label={c.activo ? 'Activo' : 'Mantenimiento'} color={c.activo ? 'success' : 'warning'} size="small"
                            sx={{ fontWeight: 600, fontSize: 11 }} />
                        </TableCell>
                        <TableCell>
                          <Button
                            size="small" variant="outlined"
                            color={c.activo ? 'error' : 'success'}
                            disabled={toggleMutation.isPending}
                            onClick={() => toggleMutation.mutate({ idCamion: c.idCamion, activo: !c.activo })}
                            sx={{ fontWeight: 600, fontSize: 11, textTransform: 'none' }}
                          >
                            {c.activo ? 'Desactivar' : 'Activar'}
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {/* Tab 2: Usuarios — solo lo funcional */}
      {tab === 2 && (
        <Box>
          <Card sx={{ mb: 3 }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '1.5rem !important' }}>
              <Avatar sx={{ bgcolor: alpha('#003527', 0.1), color: '#003527', width: 48, height: 48 }}>
                <GroupIcon />
              </Avatar>
              <Box>
                <Typography sx={{ fontSize: 11, color: '#707974', fontWeight: 500, fontFamily: 'Inter' }}>Usuarios Activos</Typography>
                <Typography sx={{ fontWeight: 700, fontSize: 24, color: '#151c27', fontFamily: 'Inter' }}>{admins.length}</Typography>
              </Box>
            </CardContent>
          </Card>

          {adminsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress /></Box>
          ) : (
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
              {admins.map((a: Admin) => (
                <Card key={a.id}>
                  <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '1.5rem !important' }}>
                    <Avatar sx={{ width: 44, height: 44, bgcolor: '#064E3B', color: '#FFFFFF', fontSize: 16, fontWeight: 700 }}>
                      {(a.nombre || 'A').charAt(0).toUpperCase()}
                    </Avatar>
                    <Box sx={{ flex: 1 }}>
                      <Typography sx={{ fontWeight: 600, color: '#151c27', fontSize: 14, fontFamily: 'Inter' }}>{a.nombre}</Typography>
                      <Typography sx={{ fontSize: 12, color: '#707974', fontFamily: 'Inter' }}>{a.email}</Typography>
                    </Box>
                    {a.activo && (
                      <Button size="small" color="error" sx={{ fontWeight: 600, fontSize: 11, textTransform: 'none' }}
                        onClick={() => desactivarMutation.mutate(a.id)}>
                        Suspender
                      </Button>
                    )}
                    {!a.activo && (
                      <Chip label="Inactivo" size="small" color="error" sx={{ fontWeight: 600, fontSize: 11 }} />
                    )}
                  </CardContent>
                </Card>
              ))}
            </Box>
          )}
        </Box>
      )}
    </Box>
    </Box>
  );
}
