package com.utp.Basurapp.Repository;

import com.utp.Basurapp.Model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
