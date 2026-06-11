package com.utp.basurapp.recolectorapp.api

import com.utp.basurapp.recolectorapp.data.NominatimResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {

    @GET("reverse")
    fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Query("zoom") zoom: Int = 18,
        @Query("accept-language") lang: String = "es"
    ): Call<NominatimResponse>
}
