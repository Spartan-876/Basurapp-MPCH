package com.utp.Basurapp.Controller;

import com.utp.Basurapp.Config.CamionUbicacionStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/camion")
public class CamionController {

    private final CamionUbicacionStore camionUbicacionStore;

    public CamionController(CamionUbicacionStore camionUbicacionStore) {
        this.camionUbicacionStore = camionUbicacionStore;
    }

    @GetMapping("/ubicacion")
    public ResponseEntity<?> obtenerUbicacion() {
        var punto = camionUbicacionStore.getUbicacionActual();

        if (punto == null) {
            return ResponseEntity.ok(Map.of(
                    "latitud", -6.8681,
                    "longitud", -79.8201,
                    "mock", true
            ));
        }

        return ResponseEntity.ok(Map.of(
                "latitud", punto.getY(),
                "longitud", punto.getX(),
                "mock", false
        ));
    }
}
