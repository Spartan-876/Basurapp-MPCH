package com.utp.basurapp.recolectorapp.api

import com.utp.basurapp.recolectorapp.data.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/auth/registrar")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @PUT("api/usuarios/fcm-token")
    fun actualizarFcmToken(@Body request: FcmTokenRequest): Call<ApiResponse>

    @GET("api/usuarios/perfil")
    fun obtenerPerfil(): Call<PerfilResponse>

    @GET("api/camiones")
    fun getCamiones(): Call<List<CamionResponse>>

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

    @PUT("api/usuarios/direccion")
    fun actualizarDireccion(@Body request: DireccionRequest): Call<ApiResponse>

    @PUT("api/usuarios/ubicacion")
    fun actualizarUbicacion(@Body request: ActualizarUbicacionRequest): Call<ApiResponse>

    @Multipart
    @POST("api/reportes")
    fun enviarReporte(
        @Part file: MultipartBody.Part,
        @Part("descripcion") descripcion: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part("direccion") direccion: RequestBody
    ): Call<ReporteResponse>

    @GET("api/reportes")
    fun listarReportes(): Call<List<ReporteResponse>>
}
