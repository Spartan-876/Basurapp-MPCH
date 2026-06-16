package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.admin.dto.AlertaManualRequest;
import com.utp.Basurapp.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/alertas")
public class AdminAlertaController {

    private final AdminService adminService;
    private final String servidorRutasUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public AdminAlertaController(AdminService adminService,
                                  @Value("${app.servidor-rutas.url}") String servidorRutasUrl) {
        this.adminService = adminService;
        this.servidorRutasUrl = servidorRutasUrl;
    }

    @PostMapping("/general")
    public ResponseEntity<?> alertaGeneral(@RequestBody Map<String, String> body) {
        String mensaje = body.get("mensaje");
        if (mensaje == null || mensaje.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El mensaje es obligatorio"));
        }

        adminService.enviarAlertaGeneral(mensaje);
        return ResponseEntity.ok(Map.of("mensaje", "Alerta general enviada a todos los ciudadanos"));
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/camion")
    public ResponseEntity<?> alertaPorCamion(@RequestBody AlertaManualRequest request) {
        if (request.getIdCamion() == null || request.getIdCamion().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El idCamion es obligatorio"));
        }
        if (request.getMensaje() == null || request.getMensaje().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El mensaje es obligatorio"));
        }
        if (request.getRadioMetros() <= 0) {
            request.setRadioMetros(1000);
        }

        String placa = request.getIdCamion();
        try {
            Object result = restTemplate.getForObject(
                    servidorRutasUrl + "/api/camion/" + request.getIdCamion() + "/ubicacion", Object.class);
            if (result instanceof Map) {
                Map<String, Object> json = (Map<String, Object>) result;
                if (json.containsKey("placa") && json.get("placa") != null) {
                    placa = json.get("placa").toString();
                }
            }
        } catch (Exception e) {
            System.err.println("[AdminAlertaController] No se pudo obtener placa: " + e.getMessage());
        }

        adminService.enviarAlertaPorCamion(
                request.getIdCamion(),
                placa,
                request.getMensaje(),
                request.getRadioMetros());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Alerta enviada para camion " + placa,
                "radio", request.getRadioMetros()));
    }
}
