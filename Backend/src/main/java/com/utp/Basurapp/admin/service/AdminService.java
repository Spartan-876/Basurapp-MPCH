package com.utp.Basurapp.admin.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.utp.Basurapp.common.model.Admin;
import com.utp.Basurapp.common.repository.AdminRepository;
import com.utp.Basurapp.common.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String servidorRutasUrl;

    public AdminService(AdminRepository adminRepository,
                        UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${app.servidor-rutas.url}") String servidorRutasUrl) {
        this.adminRepository = adminRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servidorRutasUrl = servidorRutasUrl;
    }

    public Admin login(String email, String password) {
        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            return null;
        }
        if (!admin.isActivo()) {
            return null;
        }
        return admin;
    }

    public Admin register(String nombre, String email, String password) {
        if (adminRepository.existsByEmail(email)) {
            return null;
        }
        Admin admin = new Admin();
        admin.setNombre(nombre);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setActivo(true);
        return adminRepository.save(admin);
    }

    public List<Admin> listarAdmins() {
        return adminRepository.findAll();
    }

    public Admin desactivarAdmin(Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin != null) {
            admin.setActivo(false);
            adminRepository.save(admin);
        }
        return admin;
    }

    public List<Map<String, Object>> listarCiudadanos() {
        return usuarioRepository.findAll().stream().map(u -> {
            Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", u.getId());
            dto.put("nombre", u.getNombre());
            dto.put("email", u.getEmail());
            dto.put("direccion", u.getDireccionRegistrada() != null ? u.getDireccionRegistrada() : "");
            if (u.getUbicacionCasa() != null) {
                dto.put("latitud", u.getUbicacionCasa().getY());
                dto.put("longitud", u.getUbicacionCasa().getX());
            }
            if (u.getDistrito() != null) {
                dto.put("distrito", u.getDistrito().getNombre());
            }
            return dto;
        }).toList();
    }

    public int contarCiudadanos() {
        return (int) usuarioRepository.count();
    }

    public void enviarAlertaGeneral(String mensaje) {
        List<com.utp.Basurapp.common.model.Usuario> usuarios = usuarioRepository.findAll();
        for (com.utp.Basurapp.common.model.Usuario usuario : usuarios) {
            if (usuario.getFcmToken() != null && !usuario.getFcmToken().isEmpty()) {
                enviarNotificacionFirebase(usuario.getFcmToken(), "Alerta General", mensaje);
            }
        }
    }

    public void enviarAlertaPorCamion(String idCamion, String placa, String mensaje, double radioMetros) {
        try {
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(3))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(servidorRutasUrl + "/api/camion/" + idCamion + "/ubicacion"))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = httpClient.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(response.body());
                double lat = json.path("coordenadas").path("latitud").asDouble();
                double lon = json.path("coordenadas").path("longitud").asDouble();

                if (lat != 0.0 && lon != 0.0) {
                    List<com.utp.Basurapp.common.model.Usuario> usuariosCerca =
                            usuarioRepository.encontrarUsuariosEnRadio(lat, lon, radioMetros);

                    for (com.utp.Basurapp.common.model.Usuario usuario : usuariosCerca) {
                        if (usuario.getFcmToken() != null && !usuario.getFcmToken().isEmpty()) {
                            enviarNotificacionFirebase(usuario.getFcmToken(), "Alerta " + placa, mensaje);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error enviando alerta por camion: " + e.getMessage());
        }
    }

    private void enviarNotificacionFirebase(String token, String titulo, String cuerpo) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(cuerpo)
                            .build())
                    .putData("tipo", "ALERTA_ADMIN")
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Error enviando notificacion: " + e.getMessage());
        }
    }
}
