package com.utp.Basurapp.Repository;

import com.utp.Basurapp.Model.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Point;

import java.util.Optional;

public interface DistritoRepository extends JpaRepository<Distrito, String> {

    Optional<Distrito> findByUbigeo(String ubigeo);

    @Query(value = "SELECT * FROM distritos d WHERE ST_Contains(d.geometria, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) = true LIMIT 1",
            nativeQuery = true)
    Optional<Distrito> encontrarDistritoQueContiene(
            @Param("lat") double lat,
            @Param("lon") double lon);

}
