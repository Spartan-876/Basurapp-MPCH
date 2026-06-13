package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.common.config.JwtUtil;
import com.utp.Basurapp.admin.dto.AdminLoginRequest;
import com.utp.Basurapp.admin.dto.AdminRegisterRequest;
import com.utp.Basurapp.admin.service.AdminService;
import com.utp.Basurapp.common.model.Admin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminAuthController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        Admin admin = adminService.login(request.getEmail(), request.getPassword());
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas o cuenta desactivada"));
        }

        String token = jwtUtil.generarTokenAdmin(admin.getEmail());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", admin.getEmail(),
                "nombre", admin.getNombre()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AdminRegisterRequest request, Authentication auth) {
        String emailAdmin = auth.getName();

        Admin adminCreado = adminService.register(
                request.getNombre(),
                request.getEmail(),
                request.getPassword());

        if (adminCreado == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya esta registrado"));
        }

        return ResponseEntity.ok(Map.of(
                "id", adminCreado.getId(),
                "nombre", adminCreado.getNombre(),
                "email", adminCreado.getEmail(),
                "mensaje", "Admin creado exitosamente"
        ));
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarAdmins() {
        return ResponseEntity.ok(adminService.listarAdmins().stream().map(a -> {
            Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", a.getId());
            dto.put("nombre", a.getNombre());
            dto.put("email", a.getEmail());
            dto.put("activo", a.isActivo());
            dto.put("fechaCreacion", a.getFechaCreacion());
            return dto;
        }).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarAdmin(@PathVariable Long id) {
        Admin admin = adminService.desactivarAdmin(id);
        if (admin == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admin no encontrado"));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Admin desactivado exitosamente"));
    }
}
