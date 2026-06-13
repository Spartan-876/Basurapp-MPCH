package com.utp.Basurapp.app.service;

import org.springframework.beans.factory.annotation.Value;
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

    private final AlertaService alertaService;
    private final String servidorRutasUrl;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CamionAlertScheduler(AlertaService alertaService,
                                 @Value("${app.servidor-rutas.url}") String servidorRutasUrl) {
        this.alertaService = alertaService;
        this.servidorRutasUrl = servidorRutasUrl;
    }

    @Scheduled(fixedRate = 15000)
    public void verificarProximidadCamiones() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorRutasUrl + "/api/camiones"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode camiones = objectMapper.readTree(response.body());

                if (camiones.isArray()) {
                    for (JsonNode camion : camiones) {
                        boolean activo = camion.path("activo").asBoolean(false);

                        if (activo) {
                            String idCamion = camion.path("idCamion").asText("");
                            String placa = camion.path("placa").asText("");
                            JsonNode coordenadas = camion.path("coordenadas");

                            if (coordenadas != null && !coordenadas.isNull()) {
                                double lat = coordenadas.path("latitud").asDouble();
                                double lon = coordenadas.path("longitud").asDouble();

                                if (lat != 0.0 && lon != 0.0) {
                                    alertaService.procesarUbicacionCamion(idCamion, placa, lat, lon);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ServidorRutas no disponible, ignorar silenciosamente
        }
    }
}
