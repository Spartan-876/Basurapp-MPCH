package com.utp.Basurapp.admin.dto;

import lombok.Data;

@Data
public class AdminRegisterRequest {
    private String nombre;
    private String email;
    private String password;
}
