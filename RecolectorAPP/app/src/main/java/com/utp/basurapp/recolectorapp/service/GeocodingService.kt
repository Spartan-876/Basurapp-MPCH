package com.utp.basurapp.recolectorapp.service

import com.utp.basurapp.recolectorapp.api.NominatimApi
import com.utp.basurapp.recolectorapp.data.NominatimResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GeocodingService {

    private var nominatimApi: NominatimApi? = null

    private fun getApi(): NominatimApi {
        if (nominatimApi == null) {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Basurapp/1.0 (basurapp.utp.edu.pe)")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            nominatimApi = retrofit.create(NominatimApi::class.java)
        }
        return nominatimApi!!
    }

    fun obtenerDireccion(lat: Double, lon: Double, callback: (String?) -> Unit) {
        getApi().reverseGeocode(lat, lon)
            .enqueue(object : retrofit2.Callback<NominatimResponse> {
                override fun onResponse(
                    call: retrofit2.Call<NominatimResponse>,
                    response: retrofit2.Response<NominatimResponse>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body()?.display_name)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: retrofit2.Call<NominatimResponse>, t: Throwable) {
                    callback(null)
                }
            })
    }
}
