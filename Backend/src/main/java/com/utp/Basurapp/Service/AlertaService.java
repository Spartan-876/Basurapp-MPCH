package com.utp.Basurapp.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.utp.Basurapp.Model.Usuario;
import com.utp.Basurapp.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void procesarUbicacionCamion(double lat, double lon) {
        double radioMetros = 1000.0;
        List<Usuario> usuariosCerca = usuarioRepository.encontrarUsuariosEnRadio(lat, lon, radioMetros);

        for (Usuario usuario : usuariosCerca) {
            enviarNotificacionFirebase(usuario.getFcmToken());
        }
    }

    private void enviarNotificacionFirebase(String token) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("¡Camion Recolector a la vista!")
                            .setBody("El camión está a unos 10 minutos de tu casa. ¡Prepara las bolsas!")
                            .build())
                    .putData("tipo", "ALERTA_CAMION")
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}