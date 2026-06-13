package com.utp.basurapp.recolectorapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.utp.basurapp.recolectorapp.api.RetrofitClient
import com.utp.basurapp.recolectorapp.data.ApiResponse
import com.utp.basurapp.recolectorapp.data.FcmTokenRequest
import com.utp.basurapp.recolectorapp.util.NotificationHistoryManager
import com.utp.basurapp.recolectorapp.util.SessionManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val prefs = getSharedPreferences("basurapp_session", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        val sessionToken = prefs.getString("token", null) ?: return
        val sessionManager = SessionManager(applicationContext)
        RetrofitClient.init(sessionManager)
        RetrofitClient.getApiService().actualizarFcmToken(FcmTokenRequest(token))
            .enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ApiResponse>,
                    response: retrofit2.Response<ApiResponse>
                ) {}

                override fun onFailure(
                    call: retrofit2.Call<ApiResponse>,
                    t: Throwable
                ) {}
            })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val titulo = remoteMessage.notification?.title ?: "Camion cerca!"
        val mensaje = remoteMessage.notification?.body ?: "El recolector esta por tu casa."
        mostrarNotificacion(titulo, mensaje)
    }

    private fun mostrarNotificacion(title: String, message: String) {
        val channelId = "basura_alerta"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel != null) {
                notificationManager.deleteNotificationChannel(channelId)
            }

            val channel = NotificationChannel(
                channelId,
                "Alertas Basura",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de proximidad del camion recolector"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notificacion_camion)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)

        notificationManager.notify(1, builder.build())

        NotificationHistoryManager.guardar(
            this,
            title,
            message,
            "importante"
        )
    }
}
