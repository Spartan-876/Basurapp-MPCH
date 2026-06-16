package com.utp.Basurapp.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = loadServiceAccount();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase ha sido inicializado correctamente.");
            }
        } catch (IOException e) {
            System.err.println("Error al inicializar Firebase: " + e.getMessage());
        }
    }

    private InputStream loadServiceAccount() throws IOException {
        File externalFile = new File("/app/config/firebase-service-account.json");
        if (externalFile.exists()) {
            System.out.println("Firebase: cargando desde volumen externo");
            return new FileInputStream(externalFile);
        }
        System.out.println("Firebase: cargando desde classpath");
        return new ClassPathResource("firebase-service-account.json").getInputStream();
    }
}