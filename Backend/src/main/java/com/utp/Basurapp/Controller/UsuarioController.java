package com.utp.Basurapp.Controller;

import com.utp.Basurapp.Config.CamionUbicacionStore;
import com.utp.Basurapp.Service.AlertaService;
import com.utp.Basurapp.dto.UsuarioDTO;
import com.utp.Basurapp.Model.Usuario;
import com.utp.Basurapp.Repository.UsuarioRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AlertaService alertaService;
    private final CamionUbicacionStore camionUbicacionStore;
    private final PasswordEncoder passwordEncoder;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public UsuarioController(UsuarioRepository usuarioRepository,
                             AlertaService alertaService,
                             CamionUbicacionStore camionUbicacionStore,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alertaService = alertaService;
        this.camionUbicacionStore = camionUbicacionStore;
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
        usuario.setTelefonoFamiliar(dto.getTelefonoFamiliar());

        Point punto = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
        punto.setSRID(4326);
        usuario.setUbicacionCasa(punto);

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado con exito"));
    }

    @GetMapping("/prueba-camion")
    public String probarCamion(@RequestParam double lat, @RequestParam double lon) {
        Point punto = geometryFactory.createPoint(new Coordinate(lon, lat));
        punto.setSRID(4326);
        camionUbicacionStore.actualizarUbicacion(punto);

        alertaService.procesarUbicacionCamion(lat, lon);
        return "Procesando ubicacion del camion...";
    }
}
