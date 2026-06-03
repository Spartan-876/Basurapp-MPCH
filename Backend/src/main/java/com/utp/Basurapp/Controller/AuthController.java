package com.utp.Basurapp.Controller;

import com.utp.Basurapp.Config.JwtUtil;
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
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public AuthController(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String nombre = (String) body.get("nombre");

        if (email == null || password == null || nombre == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email, password y nombre son obligatorios"));
        }

        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya esta registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);

        if (body.containsKey("fcmToken") && body.get("fcmToken") != null) {
            usuario.setFcmToken(body.get("fcmToken").toString());
        }

        if (body.containsKey("latitud") && body.containsKey("longitud")) {
            Object latObj = body.get("latitud");
            Object lonObj = body.get("longitud");
            if (latObj != null && lonObj != null) {
                try {
                    double lat = Double.parseDouble(latObj.toString());
                    double lon = Double.parseDouble(lonObj.toString());
                    Point punto = geometryFactory.createPoint(new Coordinate(lon, lat));
                    punto.setSRID(4326);
                    usuario.setUbicacionCasa(punto);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        usuarioRepository.save(usuario);

        String token = jwtUtil.generarToken(email);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "nombre", nombre
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email y password son obligatorios"));
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
        }

        if (body.containsKey("fcmToken") && body.get("fcmToken") != null) {
            usuario.setFcmToken(body.get("fcmToken").toString());
            usuarioRepository.save(usuario);
        }

        String token = jwtUtil.generarToken(email);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "nombre", usuario.getNombre()));
    }
}
