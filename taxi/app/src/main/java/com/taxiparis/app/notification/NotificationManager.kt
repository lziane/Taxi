package com.taxiparis.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.taxiparis.app.MainActivity
import com.taxiparis.app.R

class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ALERTS = "alerts_channel"
        private const val GROUP_KEY = "com.taxiparis.app.ALERTS"
        private const val ID_CDG = 1001
        private const val ID_ORY = 1002
        private const val ID_SUMMARY = 1000

        const val ACTION_OK = "com.taxiparis.app.ACTION_OK"
        const val ACTION_NO = "com.taxiparis.app.ACTION_NO"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alertes de seuil",
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifie quand les temps d'attente dépassent vos seuils"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(alertChannel)
        }
    }

    fun showAlert(airport: String, time: String, minutes: Int, vibrationEnabled: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notificationId = if (airport.contains("CDG", ignoreCase = true)) ID_CDG else ID_ORY

        if (minutes <= 0) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context, 0, contentIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val okIntent = Intent(context, NotificationActionReceiver::class.java).apply { 
            action = ACTION_OK 
            putExtra("notification_id", notificationId)
        }
        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply { 
            action = ACTION_NO 
            putExtra("notification_id", notificationId)
        }

        val okPending = PendingIntent.getBroadcast(context, 1, okIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val noPending = PendingIntent.getBroadcast(context, 2, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alerte d'attente $airport")
            .setContentText("Temps estimé : $time. Prévoyez de l'avance !")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPending)
            .setGroup(GROUP_KEY)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .addAction(0, "OK (Silence 1h)", okPending)
            .addAction(0, "Non merci (30m)", noPending)

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        }

        val summaryBuilder = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Taxi Paris Alerts")
            .setContentText("Plusieurs alertes d'attente")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        manager.notify(notificationId, builder.build())
        manager.notify(ID_SUMMARY, summaryBuilder.build())
    }

    fun dismissAlert(airport: String) {
        val notificationId = if (airport.contains("CDG", ignoreCase = true)) ID_CDG else ID_ORY
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        manager.cancel(notificationId)
    }
}
