package com.utp.basurapp.recolectorapp.api

import com.utp.basurapp.recolectorapp.data.ApiResponse
import com.utp.basurapp.recolectorapp.data.UsuarioRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/usuarios/registrar")
    fun registrarUsuario(@Body request: UsuarioRequest): Call<ApiResponse>
}