package com.utp.basurapp.recolectorapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. Mostrar la notificación en el celular
        val titulo = remoteMessage.notification?.title ?: "¡Camión cerca!"
        val mensaje = remoteMessage.notification?.body ?: "El recolector está por tu casa."
        mostrarNotificacion(titulo, mensaje)

        // 2. LOGICA DEL SMS (Opcional por ahora)
        // Aquí podrías disparar el SMS automáticamente si guardaste el número en SharedPreferences
    }

    private fun mostrarNotificacion(title: String, message: String) {
        val channelId = "basura_alerta"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas Basura", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, builder.build())
    }
}