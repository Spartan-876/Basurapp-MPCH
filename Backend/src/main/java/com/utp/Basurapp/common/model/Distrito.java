package com.utp.Basurapp.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "distritos")
@Data
public class Distrito {

    @Id
    @Column(length = 6)
    private String ubigeo;

    @Column(nullable = false)
    private String nombre;

    private String departamento;

    private String provincia;

    @Column(columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geometria;

}
