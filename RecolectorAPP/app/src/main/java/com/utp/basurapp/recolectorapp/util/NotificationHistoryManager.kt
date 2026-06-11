package com.utp.basurapp.recolectorapp.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class NotificationHistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basurapp_notification_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY = "history"
        private const val MAX_RECORDS = 50

        fun guardar(context: Context, titulo: String, descripcion: String, tipo: String, distancia: String? = null) {
            val manager = NotificationHistoryManager(context)
            val record = JSONObject().apply {
                put("titulo", titulo)
                put("descripcion", descripcion)
                put("tipo", tipo)
                put("timestamp", System.currentTimeMillis())
                put("distancia", distancia ?: JSONObject.NULL)
            }
            manager.agregar(record)
        }
    }

    private fun agregar(record: JSONObject) {
        val arr = obtenerArray()
        arr.put(record)
        while (arr.length() > MAX_RECORDS) {
            arr.remove(0)
        }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    fun obtenerHistorial(): List<NotificationRecord> {
        val arr = obtenerArray()
        val result = mutableListOf<NotificationRecord>()
        for (i in arr.length() - 1 downTo 0) {
            val obj = arr.getJSONObject(i)
            result.add(
                NotificationRecord(
                    titulo = obj.getString("titulo"),
                    descripcion = obj.getString("descripcion"),
                    tipo = obj.optString("tipo", "camion"),
                    timestamp = obj.getLong("timestamp"),
                    distancia = if (obj.isNull("distancia")) null else obj.getString("distancia")
                )
            )
        }
        return result
    }

    fun obtenerHistorialPorTipo(tipo: String): List<NotificationRecord> {
        return obtenerHistorial().filter { it.tipo == tipo }
    }

    fun limpiar() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun obtenerArray(): JSONArray {
        val str = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        return try {
            JSONArray(str)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    data class NotificationRecord(
        val titulo: String,
        val descripcion: String,
        val tipo: String,
        val timestamp: Long,
        val distancia: String?
    )
}
