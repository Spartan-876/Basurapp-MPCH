package com.utp.Basurapp.Controller;

import com.utp.Basurapp.Service.AlertaService;
import com.utp.Basurapp.dto.UsuarioDTO;
import com.utp.Basurapp.Model.Usuario;
import com.utp.Basurapp.Repository.UsuarioRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlertaService alertaService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @PostMapping("/registrar")
    public String registrarUsuario(@RequestBody UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setFcmToken(dto.getFcmToken());
        usuario.setTelefonoFamiliar(dto.getTelefonoFamiliar());

        // Convertimos lat/lon al objeto Point de PostGIS
        Point punto = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
        punto.setSRID(4326); // Estándar GPS
        usuario.setUbicacionCasa(punto);

        usuarioRepository.save(usuario);
        return "Usuario registrado con éxito";
    }

    @GetMapping("/prueba-camion")
    public String probarCamion(@RequestParam double lat, @RequestParam double lon) {
        alertaService.procesarUbicacionCamion(lat, lon);
        return "Procesando ubicación del camión...";
    }
}
