package com.utp.Basurapp.Controller;

import com.utp.Basurapp.Config.JwtUtil;
import com.utp.Basurapp.Model.Distrito;
import com.utp.Basurapp.Model.Usuario;
import com.utp.Basurapp.Repository.DistritoRepository;
import com.utp.Basurapp.Repository.UsuarioRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final DistritoRepository distritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public AuthController(UsuarioRepository usuarioRepository,
            DistritoRepository distritoRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.distritoRepository = distritoRepository;
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

        if (!body.containsKey("latitud") || !body.containsKey("longitud")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debes seleccionar tu ubicacion en el mapa"));
        }

        Double lat = parseDouble(body.get("latitud"));
        Double lon = parseDouble(body.get("longitud"));
        if (lat == null || lon == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Coordenadas invalidas"));
        }

        Optional<Distrito> distritoOpt = distritoRepository.encontrarDistritoQueContiene(lat, lon);
        if (distritoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "La ubicacion seleccionada esta fuera del distrito de Chiclayo. Ajusta el pin dentro de los limites del distrito."));
        }

        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya esta registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setDistrito(distritoOpt.get());

        Point punto = geometryFactory.createPoint(new Coordinate(lon, lat));
        punto.setSRID(4326);
        usuario.setUbicacionCasa(punto);

        if (body.containsKey("fcmToken") && body.get("fcmToken") != null) {
            usuario.setFcmToken(body.get("fcmToken").toString());
        }

        if (body.containsKey("direccion") && body.get("direccion") != null) {
            usuario.setDireccionRegistrada(body.get("direccion").toString());
        }

        usuarioRepository.save(usuario);

        String token = jwtUtil.generarToken(email);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "nombre", nombre,
                "distrito", distritoOpt.get().getNombre(),
                "latitud", lat,
                "longitud", lon,
                "direccion", usuario.getDireccionRegistrada() != null ? usuario.getDireccionRegistrada() : ""
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

        if (usuario.getUbicacionCasa() != null) {
            double lat = usuario.getUbicacionCasa().getY();
            double lon = usuario.getUbicacionCasa().getX();
            Optional<Distrito> distritoOpt = distritoRepository.encontrarDistritoQueContiene(lat, lon);
            if (distritoOpt.isEmpty()) {
                return ResponseEntity.status(403).body(Map.of("error",
                        "Tu ubicacion registrada esta fuera del distrito de Chiclayo. Vuelve a registrarte."));
            }
            if (usuario.getDistrito() == null) {
                usuario.setDistrito(distritoOpt.get());
            }
        }

        if (body.containsKey("fcmToken") && body.get("fcmToken") != null) {
            usuario.setFcmToken(body.get("fcmToken").toString());
            usuarioRepository.save(usuario);
        }

        String token = jwtUtil.generarToken(email);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("nombre", usuario.getNombre());

        if (usuario.getUbicacionCasa() != null) {
            response.put("latitud", usuario.getUbicacionCasa().getY());
            response.put("longitud", usuario.getUbicacionCasa().getX());
        }

        response.put("direccion", usuario.getDireccionRegistrada() != null ? usuario.getDireccionRegistrada() : "");

        return ResponseEntity.ok(response);
    }

    private Double parseDouble(Object o) {
        if (o == null) return null;
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
