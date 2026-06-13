package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.admin.dto.CamionEstadoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/camiones")
public class AdminCamionController {

    private static final String SERVIDOR_RUTAS_BASE = "http://localhost:3001";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @GetMapping
    public ResponseEntity<?> listarCamiones() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVIDOR_RUTAS_BASE + "/api/camiones"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return ResponseEntity.ok(response.body());
            } else {
                return ResponseEntity.status(response.statusCode()).body(response.body());
            }
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }
    }

    @PutMapping("/{idCamion}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String idCamion, @RequestBody CamionEstadoRequest request) {
        try {
            String json = "{\"activo\":" + request.isActivo() + "}";
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SERVIDOR_RUTAS_BASE + "/api/camion/" + idCamion + "/estado"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

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
