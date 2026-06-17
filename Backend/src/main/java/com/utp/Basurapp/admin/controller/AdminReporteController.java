package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.admin.dto.ReporteEstadoRequest;
import com.utp.Basurapp.common.model.Reporte;
import com.utp.Basurapp.common.repository.ReporteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reportes")
public class AdminReporteController {

    private final ReporteRepository reporteRepository;

    public AdminReporteController(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @GetMapping
    public ResponseEntity<?> listarReportes() {
        List<Reporte> reportes = reporteRepository.findAll();
        List<Map<String, Object>> dtos = new ArrayList<>();
        for (Reporte r : reportes) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", r.getId());
            dto.put("descripcion", r.getDescripcion());
            dto.put("latitud", r.getLatitud());
            dto.put("longitud", r.getLongitud());
            dto.put("direccion", r.getDireccion());
            dto.put("nombreFoto", r.getNombreFoto());
            dto.put("fecha", r.getFecha());
            dto.put("estado", r.getEstado());
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/count")
    public ResponseEntity<?> contarReportes() {
        return ResponseEntity.ok(reporteRepository.count());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> contarPendientes() {
        long count = reporteRepository.findAll().stream()
                .filter(r -> r.getEstado() == Reporte.EstadoReporte.PENDIENTE)
                .count();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody ReporteEstadoRequest request) {
        Reporte reporte = reporteRepository.findById(id).orElse(null);
        if (reporte == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reporte no encontrado"));
        }

        try {
            Reporte.EstadoReporte nuevoEstado = Reporte.EstadoReporte.valueOf(request.getEstado());
            reporte.setEstado(nuevoEstado);
            reporteRepository.save(reporte);
            return ResponseEntity.ok(Map.of(
                    "id", reporte.getId(),
                    "estado", reporte.getEstado(),
                    "mensaje", "Estado actualizado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado invalido. Use: PENDIENTE, EN_PROCESO, RESUELTO"));
        }
    }
}
