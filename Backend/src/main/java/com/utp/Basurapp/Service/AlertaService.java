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

    private final Map<String, Map<String, Integer>> notificacionesEnviadas = new HashMap<>();
    private final Map<String, Set<String>> usuariosEnZonaPorCamion = new HashMap<>();

    public void procesarUbicacionCamion(String idCamion, String placa, double lat, double lon) {
        double radioMetros = 300.0;
        List<Usuario> usuariosCerca = usuarioRepository.encontrarUsuariosEnRadio(lat, lon, radioMetros);

        Set<String> emailEnZona = new HashSet<>();

        for (Usuario usuario : usuariosCerca) {
            String email = usuario.getEmail();
            emailEnZona.add(email);

            String clave = email + "|" + idCamion;

            if (!usuariosEnZonaPorCamion.containsKey(idCamion)) {
                usuariosEnZonaPorCamion.put(idCamion, new HashSet<>());
            }
            Set<String> zonaCamion = usuariosEnZonaPorCamion.get(idCamion);

            if (!zonaCamion.contains(email)) {
                notificacionesEnviadas.computeIfAbsent(clave, k -> new HashMap<>()).put(idCamion, 0);
                zonaCamion.add(email);
            }

            Map<String, Integer> userCounts = notificacionesEnviadas.getOrDefault(clave, new HashMap<>());
            int enviadas = userCounts.getOrDefault(idCamion, 0);

            if (enviadas < MAX_NOTIFICACIONES_POR_SESION) {
                System.out.println("Alertado: " + usuario.getNombre() + " por camion " + placa + " (#" + (enviadas + 1) + "/" + MAX_NOTIFICACIONES_POR_SESION + ")");
                enviarNotificacionFirebase(usuario.getFcmToken(), placa);
                userCounts.put(idCamion, enviadas + 1);
                notificacionesEnviadas.put(clave, userCounts);
            }
        }

        Set<String> zonaCamion = usuariosEnZonaPorCamion.get(idCamion);
        if (zonaCamion != null) {
            for (String email : zonaCamion) {
                if (!emailEnZona.contains(email)) {
                    String clave = email + "|" + idCamion;
                    Map<String, Integer> userCounts = notificacionesEnviadas.get(clave);
                    if (userCounts != null) {
                        userCounts.remove(idCamion);
                        if (userCounts.isEmpty()) {
                            notificacionesEnviadas.remove(clave);
                        }
                    }
                }
            }
            zonaCamion.retainAll(emailEnZona);
        }
    }

    private void enviarNotificacionFirebase(String token, String placa) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("Camion " + placa + " a la vista!")
                            .setBody("El camion " + placa + " esta pasando por tu zona. Prepara las bolsas!")
                            .build())
                    .putData("tipo", "ALERTA_CAMION")
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}
