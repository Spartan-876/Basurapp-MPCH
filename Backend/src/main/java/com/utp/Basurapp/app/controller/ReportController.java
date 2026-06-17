package com.utp.Basurapp.app.controller;

import com.utp.Basurapp.common.model.Distrito;
import com.utp.Basurapp.common.model.Reporte;
import com.utp.Basurapp.common.model.Usuario;
import com.utp.Basurapp.common.repository.DistritoRepository;
import com.utp.Basurapp.common.repository.ReporteRepository;
import com.utp.Basurapp.common.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DistritoRepository distritoRepository;
    private static final String UPLOAD_DIR = "FotosReportes";

    public ReportController(ReporteRepository reporteRepository,
                            UsuarioRepository usuarioRepository,
                            DistritoRepository distritoRepository) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.distritoRepository = distritoRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearReporte(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "descripcion", defaultValue = "") String descripcion,
            @RequestParam("latitud") double latitud,
            @RequestParam("longitud") double longitud,
            @RequestParam(value = "direccion", defaultValue = "") String direccion,
            Authentication auth) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La foto es obligatoria"));
        }

        Optional<Distrito> distritoOpt = distritoRepository.encontrarDistritoQueContiene(latitud, longitud);
        if (distritoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "La ubicacion del reporte esta fuera del distrito de Chiclayo."));
        }

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String nombreFoto = guardarFoto(file);
        if (nombreFoto == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al guardar la foto"));
        }

        Reporte reporte = new Reporte();
        reporte.setUsuario(usuario);
        reporte.setDescripcion(descripcion);
        reporte.setLatitud(latitud);
        reporte.setLongitud(longitud);
        reporte.setDireccion(direccion);
        reporte.setNombreFoto(nombreFoto);
        reporte.setEstado(Reporte.EstadoReporte.PENDIENTE);

        reporteRepository.save(reporte);

        Map<String, Object> response = new HashMap<>();
        response.put("id", reporte.getId());
        response.put("fecha", reporte.getFecha());
        response.put("estado", reporte.getEstado());
        response.put("nombreFoto", nombreFoto);
        response.put("mensaje", "Reporte enviado exitosamente");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> listarReportes(Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Reporte> reportes = reporteRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
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

    private String guardarFoto(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String nombreFoto = "reporte_" + timestamp + extension;

            Path filePath = uploadPath.resolve(nombreFoto);
            file.transferTo(filePath.toFile());

            return nombreFoto;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
