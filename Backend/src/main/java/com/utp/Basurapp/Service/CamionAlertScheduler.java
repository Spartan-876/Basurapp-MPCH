package com.utp.Basurapp.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CamionAlertScheduler {

    private static final String SERVIDOR_RUTAS_URL = "http://localhost:3001/api/camion/ubicacion-actual";
    private final AlertaService alertaService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CamionAlertScheduler(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Scheduled(fixedRate = 15000)
    public void verificarProximidadCamion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVIDOR_RUTAS_URL))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                boolean activo = json.path("activo").asBoolean(false);

                if (activo) {
                    JsonNode coordenadas = json.path("coordenadas");
                    double lat = coordenadas.path("latitud").asDouble();
                    double lon = coordenadas.path("longitud").asDouble();

                    if (lat != 0.0 && lon != 0.0) {
                        alertaService.procesarUbicacionCamion(lat, lon);
                    }
                }
            }
        } catch (Exception e) {
            // ServidorRutas no disponible, ignorar silenciosamente
        }
    }
}
