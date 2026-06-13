package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.admin.dto.CamionEstadoRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/camiones")
public class AdminCamionController {

    private final String servidorRutasUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public AdminCamionController(@Value("${app.servidor-rutas.url}") String servidorRutasUrl) {
        this.servidorRutasUrl = servidorRutasUrl;
    }

    @GetMapping
    public ResponseEntity<?> listarCamiones() {
        try {
            Object result = restTemplate.getForObject(servidorRutasUrl + "/api/camiones", Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("[AdminCamionController] ERROR: " + e.getMessage());
            return ResponseEntity.ok("[]");
        }
    }

    @PutMapping("/{idCamion}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String idCamion, @RequestBody CamionEstadoRequest request) {
        try {
            Map<String, Object> body = Map.of("activo", request.isActivo());
            restTemplate.put(servidorRutasUrl + "/api/camion/" + idCamion + "/estado", body);
            return ResponseEntity.ok(Map.of(
                    "idCamion", idCamion,
                    "activo", request.isActivo(),
                    "mensaje", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "idCamion", idCamion,
                    "activo", request.isActivo(),
                    "mensaje", "Estado actualizado (local)"));
        }
    }
}
