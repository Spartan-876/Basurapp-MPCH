package com.utp.basurapp.recolectorapp.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basurapp_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_UBICACION_REGISTRADA = "ubicacion_registrada"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_LATITUD = "latitud"
        private const val KEY_LONGITUD = "longitud"
    }

    fun guardarSesion(token: String, email: String, nombre: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_EMAIL, email)
            putString(KEY_NOMBRE, nombre)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getNombre(): String? = prefs.getString(KEY_NOMBRE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun guardarFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String? = prefs.getString(KEY_FCM_TOKEN, null)

    fun isUbicacionRegistrada(): Boolean =
        prefs.getBoolean(KEY_UBICACION_REGISTRADA, false)

    fun setUbicacionRegistrada(registrada: Boolean) {
        prefs.edit().putBoolean(KEY_UBICACION_REGISTRADA, registrada).apply()
    }

    fun guardarCoordenadas(lat: Double, lon: Double) {
        prefs.edit().apply {
            putFloat(KEY_LATITUD, lat.toFloat())
            putFloat(KEY_LONGITUD, lon.toFloat())
            apply()
        }
    }

    fun getLatitud(): Double = prefs.getFloat(KEY_LATITUD, -6.8681f).toDouble()

    fun getLongitud(): Double = prefs.getFloat(KEY_LONGITUD, -79.8201f).toDouble()

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
