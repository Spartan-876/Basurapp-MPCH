package com.utp.basurapp.recolectorapp.data

data class AuthResponse(
    val token: String?,
    val email: String?,
    val nombre: String?,
    val error: String?
)
