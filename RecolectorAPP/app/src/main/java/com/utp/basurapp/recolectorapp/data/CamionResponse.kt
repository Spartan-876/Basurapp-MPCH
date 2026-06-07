package com.utp.basurapp.recolectorapp.data

data class CamionResponse(
    val idCamion: String? = null,
    val placa: String? = null,
    val activo: Boolean = true,
    val ultimaActualizacion: String? = null,
    val coordenadas: CoordenadasCamion? = null,
    val mensaje: String? = null
)

data class CoordenadasCamion(
    val latitud: Double,
    val longitud: Double
)
