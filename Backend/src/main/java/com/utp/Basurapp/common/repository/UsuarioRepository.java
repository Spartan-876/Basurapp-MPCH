package com.utp.Basurapp.common.repository;

import com.utp.Basurapp.common.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM usuarios u WHERE " +
            "ST_DWithin(u.ubicacion_casa::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radioMetros)",
            nativeQuery = true)
    List<Usuario> encontrarUsuariosEnRadio(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radioMetros") double radioMetros);
}
