package com.utp.basurapp.recolectorapp.api

import com.utp.basurapp.recolectorapp.data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/auth/registrar")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @PUT("api/usuarios/fcm-token")
    fun actualizarFcmToken(@Body request: FcmTokenRequest): Call<ApiResponse>

    @GET("api/usuarios/perfil")
    fun obtenerPerfil(): Call<PerfilResponse>

    @GET("api/camion/ubicacion")
    fun getCamionUbicacion(): Call<CamionResponse>

    @GET("api/usuarios/familiares")
    fun listarFamiliares(): Call<List<FamiliarResponse>>

    @POST("api/usuarios/familiares")
    fun agregarFamiliar(@Body request: FamiliarRequest): Call<FamiliarResponse>

    @PUT("api/usuarios/familiares/{id}")
    fun actualizarFamiliar(@Path("id") id: Long, @Body request: FamiliarRequest): Call<FamiliarResponse>

    @DELETE("api/usuarios/familiares/{id}")
    fun eliminarFamiliar(@Path("id") id: Long): Call<ApiResponse>
}
