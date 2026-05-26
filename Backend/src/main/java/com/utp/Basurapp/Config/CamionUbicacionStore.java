package com.utp.Basurapp.Config;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class CamionUbicacionStore {

    private Point ubicacionActual;
    private long ultimaActualizacion;

    public void actualizarUbicacion(Point punto) {
        this.ubicacionActual = punto;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public Point getUbicacionActual() {
        return ubicacionActual;
    }

    public long getUltimaActualizacion() {
        return ultimaActualizacion;
    }
}
