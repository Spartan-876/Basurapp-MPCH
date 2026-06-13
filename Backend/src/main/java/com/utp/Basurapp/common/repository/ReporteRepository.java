package com.utp.Basurapp.common.repository;

import com.utp.Basurapp.common.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
