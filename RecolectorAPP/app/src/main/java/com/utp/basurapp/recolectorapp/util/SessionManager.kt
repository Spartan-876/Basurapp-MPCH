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
        private const val KEY_DIRECCION = "direccion"
        private const val KEY_RADIO_ALERTAS = "radio_alertas"
        private const val KEY_ALERTAS_ACTIVADAS = "alertas_activadas"
        private const val KEY_VIBRACION_ACTIVADA = "vibracion_activada"
        private const val KEY_SONIDO_ACTIVADO = "sonido_activado"
        private const val KEY_DIAS_ACTIVOS = "dias_activos"
        private const val KEY_TEMA = "tema"
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
            putString(KEY_LATITUD, lat.toString())
            putString(KEY_LONGITUD, lon.toString())
            apply()
        }
    }

    fun getLatitud(): Double = (prefs.getString(KEY_LATITUD, null) ?: "-6.8681").toDouble()

    fun getLongitud(): Double = (prefs.getString(KEY_LONGITUD, null) ?: "-79.8201").toDouble()

    fun guardarDireccion(direccion: String) {
        prefs.edit().putString(KEY_DIRECCION, direccion).apply()
    }

    fun getDireccion(): String = prefs.getString(KEY_DIRECCION, "") ?: ""

    fun guardarRadioAlertas(radioMetros: Int) {
        prefs.edit().putInt(KEY_RADIO_ALERTAS, radioMetros).apply()
    }

    fun getRadioAlertas(): Int = prefs.getInt(KEY_RADIO_ALERTAS, 250)

    fun setAlertasActivadas(activadas: Boolean) {
        prefs.edit().putBoolean(KEY_ALERTAS_ACTIVADAS, activadas).apply()
    }

    fun isAlertasActivadas(): Boolean = prefs.getBoolean(KEY_ALERTAS_ACTIVADAS, true)

    fun setVibracionActivada(activada: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRACION_ACTIVADA, activada).apply()
    }

    fun isVibracionActivada(): Boolean = prefs.getBoolean(KEY_VIBRACION_ACTIVADA, true)

    fun setSonidoActivado(activado: Boolean) {
        prefs.edit().putBoolean(KEY_SONIDO_ACTIVADO, activado).apply()
    }

    fun isSonidoActivado(): Boolean = prefs.getBoolean(KEY_SONIDO_ACTIVADO, true)

    fun guardarDiasActivos(dias: Set<Int>) {
        val sb = StringBuilder()
        dias.forEach { if (sb.isNotEmpty()) sb.append(","); sb.append(it) }
        prefs.edit().putString(KEY_DIAS_ACTIVOS, sb.toString()).apply()
    }

    fun getDiasActivos(): Set<Int> {
        val str = prefs.getString(KEY_DIAS_ACTIVOS, "0,1,2,3,4") ?: "0,1,2,3,4"
        return if (str.isEmpty()) emptySet() else str.split(",").map { it.trim().toInt() }.toSet()
    }

    fun isDiaActivo(dia: Int): Boolean = getDiasActivos().contains(dia)

    fun guardarTema(tema: String) {
        prefs.edit().putString(KEY_TEMA, tema).apply()
    }

    fun getTema(): String = prefs.getString(KEY_TEMA, "system") ?: "system"

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
