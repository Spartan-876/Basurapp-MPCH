package com.utp.Basurapp.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String fcmToken;

    private String telefonoFamiliar;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point ubicacionCasa;

}