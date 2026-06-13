package com.utp.basurapp.recolectorapp.data

data class ReporteResponse(
    val id: Long,
    val fecha: String,
    val descripcion: String?,
    val latitud: Double,
    val longitud: Double,
    val direccion: String?,
    val nombreFoto: String,
    val estado: String,
    val mensaje: String?
)
