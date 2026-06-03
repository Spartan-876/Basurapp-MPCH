package com.utp.basurapp.recolectorapp.data

data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val fcmToken: String? = null
)
