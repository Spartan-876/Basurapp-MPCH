package com.utp.Basurapp.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.utp.Basurapp.Model.Usuario;
import com.utp.Basurapp.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AlertaService {

    private static final int MAX_NOTIFICACIONES_POR_SESION = 2;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final Map<String, Integer> notificacionesEnviadas = new HashMap<>();
    private final Set<String> usuariosEnZona = new HashSet<>();

    public void procesarUbicacionCamion(double lat, double lon) {
        double radioMetros = 500.0;
        List<Usuario> usuariosCerca = usuarioRepository.encontrarUsuariosEnRadio(lat, lon, radioMetros);

        Set<String> emailEnZona = new HashSet<>();

        for (Usuario usuario : usuariosCerca) {
            String email = usuario.getEmail();
            emailEnZona.add(email);

            if (!usuariosEnZona.contains(email)) {
                notificacionesEnviadas.put(email, 0);
                usuariosEnZona.add(email);
            }

            int enviadas = notificacionesEnviadas.getOrDefault(email, 0);
            if (enviadas < MAX_NOTIFICACIONES_POR_SESION) {
                System.out.println("Alertado: " + usuario.getNombre() + " (#" + (enviadas + 1) + "/" + MAX_NOTIFICACIONES_POR_SESION + ")");
                enviarNotificacionFirebase(usuario.getFcmToken());
                notificacionesEnviadas.put(email, enviadas + 1);
            }
        }

        for (String email : usuariosEnZona) {
            if (!emailEnZona.contains(email)) {
                notificacionesEnviadas.remove(email);
            }
        }
        usuariosEnZona.retainAll(emailEnZona);
    }

    private void enviarNotificacionFirebase(String token) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("¡Camion Recolector a la vista!")
                            .setBody("El camion recolector esta pasando por tu zona. ¡Prepara las bolsas!")
                            .build())
                    .putData("tipo", "ALERTA_CAMION")
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}
