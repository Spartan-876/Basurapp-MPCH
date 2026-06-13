package com.utp.Basurapp.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CamionController {

    private final String servidorRutasUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public CamionController(@Value("${app.servidor-rutas.url}") String servidorRutasUrl) {
        this.servidorRutasUrl = servidorRutasUrl;
    }

    @GetMapping("/camiones")
    public ResponseEntity<?> listarCamiones() {
        try {
            Object result = restTemplate.getForObject(servidorRutasUrl + "/api/camiones", Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }
    }

    @GetMapping("/camion/{idCamion}/ubicacion")
    public ResponseEntity<?> obtenerUbicacionCamion(@PathVariable String idCamion) {
        try {
            Object result = restTemplate.getForObject(
                    servidorRutasUrl + "/api/camion/" + idCamion + "/ubicacion", Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "idCamion", idCamion,
                    "activo", false,
                    "mensaje", "Servidor GPS no disponible"
            ));
        }
    }

    @GetMapping("/camion/ubicacion")
    public ResponseEntity<?> obtenerUbicacion() {
        try {
            Object result = restTemplate.getForObject(
                    servidorRutasUrl + "/api/camion/ubicacion-actual", Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "idCamion", "SINConexion",
                    "activo", false,
                    "mensaje", "Servidor GPS no disponible"
            ));
        }
    }
}
