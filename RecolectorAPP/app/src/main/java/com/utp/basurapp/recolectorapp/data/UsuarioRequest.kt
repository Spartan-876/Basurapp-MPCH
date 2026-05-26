package com.utp.basurapp.recolectorapp.data

data class UsuarioRequest(
    val nombre: String,
    val email: String? = null,
    val password: String? = null,
    val fcmToken: String,
    val telefonoFamiliar: String,
    val latitud: Double,
    val longitud: Double
)
