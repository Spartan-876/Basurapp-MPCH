package com.utp.Basurapp.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private String nombre;
    private String email;
    private String password;
    private String fcmToken;
    private double latitud;
    private double longitud;
}
