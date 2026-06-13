package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.admin.dto.AlertaManualRequest;
import com.utp.Basurapp.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/alertas")
public class AdminAlertaController {

    private final AdminService adminService;

    public AdminAlertaController(AdminService adminService) {
        this.adminService = adminService;
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

        adminService.enviarAlertaPorCamion(
                request.getIdCamion(),
                request.getIdCamion(),
                request.getMensaje(),
                request.getRadioMetros());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Alerta enviada para camion " + request.getIdCamion(),
                "radio", request.getRadioMetros()));
    }
}
