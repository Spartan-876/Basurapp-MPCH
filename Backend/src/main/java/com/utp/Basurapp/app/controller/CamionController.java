package com.utp.Basurapp.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CamionController {

    private static final String SERVIDOR_RUTAS_BASE = "http://localhost:3001";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @GetMapping("/camiones")
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

    @GetMapping("/camion/{idCamion}/ubicacion")
    public ResponseEntity<?> obtenerUbicacionCamion(@PathVariable String idCamion) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVIDOR_RUTAS_BASE + "/api/camion/" + idCamion + "/ubicacion"))
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
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVIDOR_RUTAS_BASE + "/api/camion/ubicacion-actual"))
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
            return ResponseEntity.ok(Map.of(
                    "idCamion", "SINConexion",
                    "activo", false,
                    "mensaje", "Servidor GPS no disponible"
            ));
        }
    }
}
