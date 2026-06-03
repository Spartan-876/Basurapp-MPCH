package com.utp.Basurapp.Repository;

import com.utp.Basurapp.Model.Familiar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamiliarRepository extends JpaRepository<Familiar, Long> {

    List<Familiar> findByUsuarioIdOrderByNombreAsc(Long usuarioId);

}
