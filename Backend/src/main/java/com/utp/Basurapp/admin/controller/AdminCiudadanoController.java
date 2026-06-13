package com.utp.Basurapp.admin.controller;

import com.utp.Basurapp.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ciudadanos")
public class AdminCiudadanoController {

    private final AdminService adminService;

    public AdminCiudadanoController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<?> listarCiudadanos() {
        return ResponseEntity.ok(adminService.listarCiudadanos());
    }

    @GetMapping("/count")
    public ResponseEntity<?> contarCiudadanos() {
        return ResponseEntity.ok(adminService.contarCiudadanos());
    }
}
