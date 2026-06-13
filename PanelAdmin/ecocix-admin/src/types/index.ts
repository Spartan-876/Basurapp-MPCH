export interface Admin {
  id: number;
  nombre: string;
  email: string;
  activo: boolean;
  fechaCreacion: string;
}

export interface Ciudadano {
  id: number;
  nombre: string;
  email: string;
  latitud: number;
  longitud: number;
  direccion: string;
  fechaRegistro: string;
}

export interface Reporte {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  descripcion: string;
  latitud: number;
  longitud: number;
  direccion: string;
  nombreFoto: string;
  fecha: string;
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'RESUELTO';
}

export interface Camion {
  idCamion: string;
  placa: string;
  activo: boolean;
  ultimaActualizacion: string;
  coordenadas?: { latitud: number; longitud: number };
}

export interface StatsReportes {
  total: number;
  pendientes: number;
  enProceso: number;
  resueltos: number;
}

export interface StatsReportesPorDia {
  fecha: string;
  cantidad: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  nombre: string;
}

export interface AlertaRequest {
  mensaje: string;
  radioMetros: number;
  idCamion?: string;
}
