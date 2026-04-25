package com.utp.Basurapp.Repository;

import com.utp.Basurapp.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query(value = "SELECT * FROM usuarios u WHERE " +
            "ST_DWithin(u.ubicacion_casa, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radioMetros)",
            nativeQuery = true)
    List<Usuario> encontrarUsuariosEnRadio(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radioMetros") double radioMetros);
}
