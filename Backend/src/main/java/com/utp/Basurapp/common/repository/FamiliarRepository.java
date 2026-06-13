package com.utp.Basurapp.common.repository;

import com.utp.Basurapp.common.model.Familiar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamiliarRepository extends JpaRepository<Familiar, Long> {

    List<Familiar> findByUsuarioIdOrderByNombreAsc(Long usuarioId);

}
