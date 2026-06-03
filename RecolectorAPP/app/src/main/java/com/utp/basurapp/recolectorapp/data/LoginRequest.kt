package com.utp.basurapp.recolectorapp.data

data class LoginRequest(
    val email: String,
    val password: String,
    val fcmToken: String? = null
)
