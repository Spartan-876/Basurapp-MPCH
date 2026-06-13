import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
  Box, Typography, Card, CardContent, TextField, Button, Alert,
  Select, MenuItem, FormControl, InputLabel, Slider, CircularProgress,
  ToggleButton, ToggleButtonGroup,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import SendIcon from '@mui/icons-material/Send';
import { adminService } from '../services/api';

export default function AlertasPage() {
  const [tipo, setTipo] = useState<'general' | 'camion'>('general');
  const [mensaje, setMensaje] = useState('');
  const [idCamion, setIdCamion] = useState('');
  const [radio, setRadio] = useState(250);

  const { data: camiones = [] } = useQuery({
    queryKey: ['camiones'],
    queryFn: adminService.getCamiones,
    enabled: tipo === 'camion',
  });

  const mutation = useMutation({
    mutationFn: () => {
      if (tipo === 'general') {
        return adminService.enviarAlertaGeneral(mensaje);
      }
      return adminService.enviarAlertaCamion(idCamion, mensaje, radio);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.reset();
    mutation.mutate();
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>Alertas</Typography>

      <Card>
        <CardContent>
          <ToggleButtonGroup
            value={tipo} exclusive fullWidth
            onChange={(_, v) => { if (v) setTipo(v); }}
            sx={{ mb: 3 }}
          >
            <ToggleButton value="general">
              <NotificationsIcon sx={{ mr: 1 }} /> Alerta General
            </ToggleButton>
            <ToggleButton value="camion">
              <LocalShippingIcon sx={{ mr: 1 }} /> Alerta por Camión
            </ToggleButton>
          </ToggleButtonGroup>

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth multiline rows={4} label="Mensaje de la alerta" margin="normal"
              value={mensaje} onChange={e => setMensaje(e.target.value)}
              placeholder="Ej: Se informa que el servicio de recolección..."
              required
            />

            {tipo === 'camion' && (
              <Box sx={{ mt: 2 }}>
                <FormControl fullWidth margin="normal">
                  <InputLabel>Seleccionar camión</InputLabel>
                  <Select value={idCamion} label="Seleccionar camión"
                    onChange={e => setIdCamion(e.target.value)} required>
                    <MenuItem value="">
                      <em>-- Seleccionar camión --</em>
                    </MenuItem>
                    {camiones.map((c) => (
                      <MenuItem key={c.idCamion} value={c.idCamion}>
                        {c.idCamion} — {c.placa} {c.activo ? '(Activo)' : '(Inactivo)'}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <Box sx={{ mt: 2, px: 2 }}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Radio de alcance: {radio}m
                  </Typography>
                  <Slider
                    value={radio} onChange={(_, v) => setRadio(v as number)}
                    min={50} max={1000} step={50} valueLabelDisplay="auto"
                    valueLabelFormat={(v) => `${v}m`}
                  />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption" color="text.secondary">50m</Typography>
                    <Typography variant="caption" color="text.secondary">1000m</Typography>
                  </Box>
                </Box>
              </Box>
            )}

            {mutation.isSuccess && (
              <Alert severity="success" sx={{ mt: 2 }}>
                Alerta enviada{tipo === 'camion' ? ` a ciudadanos dentro de ${radio}m del camión ${idCamion}` : ' a todos los ciudadanos'}
              </Alert>
            )}
            {mutation.isError && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {(mutation.error as any)?.response?.data?.error || 'Error al enviar alerta'}
              </Alert>
            )}

            <Button
              fullWidth variant="contained" type="submit" size="large" sx={{ mt: 3 }}
              disabled={mutation.isPending || !mensaje}
              startIcon={mutation.isPending ? <CircularProgress size={20} /> : <SendIcon />}
            >
              {mutation.isPending ? 'Enviando...' : 'Enviar Alerta'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
