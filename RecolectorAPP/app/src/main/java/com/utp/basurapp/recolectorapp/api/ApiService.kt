package com.utp.basurapp.recolectorapp.api

import com.utp.basurapp.recolectorapp.data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/usuarios/registrar")
    fun registrarUsuario(@Body request: UsuarioRequest): Call<ApiResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/auth/registrar")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @GET("api/camion/ubicacion")
    fun getCamionUbicacion(): Call<CamionResponse>
}
