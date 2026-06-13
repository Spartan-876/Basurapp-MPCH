import api from '../config/axios';
import type { Ciudadano, Reporte, Camion, StatsReportes, StatsReportesPorDia } from '../types';

export const adminService = {
  // Ciudadanos
  getCiudadanos: () => api.get<Ciudadano[]>('/api/admin/ciudadanos').then(r => r.data),
  getCiudadanosCount: () => api.get<number>('/api/admin/ciudadanos/count').then(r => r.data),

  // Reportes
  getReportes: () => api.get<Reporte[]>('/api/admin/reportes').then(r => r.data),
  getStatsReportes: async (): Promise<StatsReportes> => {
    const reportes = await api.get<Reporte[]>('/api/admin/reportes').then(r => r.data);
    return {
      total: reportes.length,
      pendientes: reportes.filter(r => r.estado === 'PENDIENTE').length,
      enProceso: reportes.filter(r => r.estado === 'EN_PROCESO').length,
      resueltos: reportes.filter(r => r.estado === 'RESUELTO').length,
    };
  },
  getReportesPorDia: async (): Promise<StatsReportesPorDia[]> => {
    const reportes = await api.get<Reporte[]>('/api/admin/reportes').then(r => r.data);
    const counts: Record<string, number> = {};
    reportes.forEach(r => {
      const fecha = r.fecha.split('T')[0];
      counts[fecha] = (counts[fecha] || 0) + 1;
    });
    return Object.entries(counts)
      .map(([fecha, cantidad]) => ({ fecha, cantidad }))
      .sort((a, b) => a.fecha.localeCompare(b.fecha));
  },
  cambiarEstadoReporte: (id: number, estado: string) =>
    api.put(`/api/admin/reportes/${id}/estado`, { estado }).then(r => r.data),

  // Camiones
  getCamiones: () => api.get<Camion[]>('/api/admin/camiones').then(r => r.data),
  toggleCamion: (idCamion: string, activo: boolean) =>
    api.put(`/api/admin/camiones/${idCamion}/estado`, { activo }).then(r => r.data),

  // Alertas
  enviarAlertaGeneral: (mensaje: string) =>
    api.post('/api/admin/alertas/general', { mensaje }).then(r => r.data),
  enviarAlertaCamion: (idCamion: string, mensaje: string, radioMetros: number) =>
    api.post('/api/admin/alertas/camion', { idCamion, mensaje, radioMetros }).then(r => r.data),

  // Auth
  listarAdmins: () => api.get('/api/admin/auth/listar').then(r => r.data),
  registrarAdmin: (nombre: string, email: string, password: string) =>
    api.post('/api/admin/auth/register', { nombre, email, password }).then(r => r.data),
  desactivarAdmin: (id: number) =>
    api.delete(`/api/admin/auth/${id}`).then(r => r.data),
};
