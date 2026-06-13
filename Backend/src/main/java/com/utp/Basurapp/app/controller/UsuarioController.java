package com.utp.Basurapp.app.controller;

import com.utp.Basurapp.common.dto.FamiliarDTO;
import com.utp.Basurapp.common.dto.UsuarioDTO;
import com.utp.Basurapp.common.model.Distrito;
import com.utp.Basurapp.common.model.Familiar;
import com.utp.Basurapp.common.model.Usuario;
import com.utp.Basurapp.common.repository.DistritoRepository;
import com.utp.Basurapp.common.repository.FamiliarRepository;
import com.utp.Basurapp.common.repository.UsuarioRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final FamiliarRepository familiarRepository;
    private final DistritoRepository distritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public UsuarioController(UsuarioRepository usuarioRepository,
            FamiliarRepository familiarRepository,
            DistritoRepository distritoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.familiarRepository = familiarRepository;
        this.distritoRepository = distritoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioDTO dto) {
        Usuario usuario;

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            usuario = usuarioRepository.findByEmail(dto.getEmail()).orElse(new Usuario());
            if (usuario.getId() == null) {
                usuario.setEmail(dto.getEmail());
                usuario.setNombre(dto.getNombre());
                usuario.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "default"));
            }
        } else {
            usuario = new Usuario();
            usuario.setNombre(dto.getNombre());
        }

        usuario.setFcmToken(dto.getFcmToken());

        Point punto = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
        punto.setSRID(4326);
        usuario.setUbicacionCasa(punto);

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado con exito"));
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("nombre", usuario.getNombre());
        response.put("email", usuario.getEmail());

        if (usuario.getUbicacionCasa() != null) {
            response.put("latitud", usuario.getUbicacionCasa().getY());
            response.put("longitud", usuario.getUbicacionCasa().getX());    
        }

        if (usuario.getDistrito() != null) {
            response.put("distrito", usuario.getDistrito().getNombre());
        }

        response.put("direccion", usuario.getDireccionRegistrada() != null ? usuario.getDireccionRegistrada() : "");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<?> actualizarFcmToken(@RequestBody Map<String, String> body, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String fcmToken = body.get("fcmToken");
        if (fcmToken != null) {
            usuario.setFcmToken(fcmToken);
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok(Map.of("message", "Token actualizado"));
    }

    @PutMapping("/direccion")
    public ResponseEntity<?> actualizarDireccion(@RequestBody Map<String, String> body, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String direccion = body.get("direccion");
        if (direccion == null || direccion.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "direccion es obligatoria"));
        }

        usuario.setDireccionRegistrada(direccion.trim());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Direccion actualizada", "direccion", direccion.trim()));
    }

    @PutMapping("/ubicacion")
    public ResponseEntity<?> actualizarUbicacion(@RequestBody Map<String, Object> body, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Double lat = null;
        Double lon = null;
        if (body.containsKey("latitud") && body.containsKey("longitud")) {
            try {
                lat = Double.parseDouble(body.get("latitud").toString());
                lon = Double.parseDouble(body.get("longitud").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Coordenadas invalidas"));
            }
        }

        if (lat == null || lon == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "latitud y longitud son obligatorias"));
        }

        Optional<Distrito> distritoOpt = distritoRepository.encontrarDistritoQueContiene(lat, lon);
        if (distritoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "La ubicacion seleccionada esta fuera del distrito de Chiclayo."));
        }

        Point punto = geometryFactory.createPoint(new Coordinate(lon, lat));
        punto.setSRID(4326);
        usuario.setUbicacionCasa(punto);
        usuario.setDistrito(distritoOpt.get());

        if (body.containsKey("direccion") && body.get("direccion") != null) {
            usuario.setDireccionRegistrada(body.get("direccion").toString().trim());
        }

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of(
                "message", "Ubicacion actualizada",
                "latitud", lat,
                "longitud", lon,
                "direccion", usuario.getDireccionRegistrada() != null ? usuario.getDireccionRegistrada() : ""
        ));
    }

    @GetMapping("/familiares")
    public ResponseEntity<?> listarFamiliares(Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Familiar> familiares = familiarRepository.findByUsuarioIdOrderByNombreAsc(usuario.getId());
        List<FamiliarDTO> dtos = familiares.stream().map(f -> {
            FamiliarDTO dto = new FamiliarDTO();
            dto.setId(f.getId());
            dto.setNombre(f.getNombre());
            dto.setTelefono(f.getTelefono());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/familiares")
    public ResponseEntity<?> agregarFamiliar(@RequestBody FamiliarDTO body, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (body.getNombre() == null || body.getNombre().isEmpty() ||
                body.getTelefono() == null || body.getTelefono().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "nombre y telefono son obligatorios"));
        }

        Familiar familiar = new Familiar();
        familiar.setUsuario(usuario);
        familiar.setNombre(body.getNombre().trim());
        familiar.setTelefono(body.getTelefono().trim());

        familiarRepository.save(familiar);

        FamiliarDTO dto = new FamiliarDTO();
        dto.setId(familiar.getId());
        dto.setNombre(familiar.getNombre());
        dto.setTelefono(familiar.getTelefono());

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/familiares/{id}")
    public ResponseEntity<?> actualizarFamiliar(@PathVariable Long id, @RequestBody FamiliarDTO body, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Familiar familiar = familiarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado"));

        if (!familiar.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para modificar este familiar"));
        }

        if (body.getNombre() != null && !body.getNombre().isEmpty()) {
            familiar.setNombre(body.getNombre().trim());
        }
        if (body.getTelefono() != null && !body.getTelefono().isEmpty()) {
            familiar.setTelefono(body.getTelefono().trim());
        }

        familiarRepository.save(familiar);

        FamiliarDTO dto = new FamiliarDTO();
        dto.setId(familiar.getId());
        dto.setNombre(familiar.getNombre());
        dto.setTelefono(familiar.getTelefono());

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/familiares/{id}")
    public ResponseEntity<?> eliminarFamiliar(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Familiar familiar = familiarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado"));

        if (!familiar.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar este familiar"));
        }

        familiarRepository.delete(familiar);
        return ResponseEntity.ok(Map.of("message", "Familiar eliminado con exito"));
    }
}
