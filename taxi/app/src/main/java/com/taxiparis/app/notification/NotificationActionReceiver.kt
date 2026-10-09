package com.taxiparis.app.notification

import android.app.NotificationManager as AndroidNotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taxiparis.app.data.local.TaxiPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = TaxiPrefs(context)
        val notificationId = intent.getIntExtra("notification_id", 0)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        manager.cancel(notificationId)

        val silenceDuration = when (intent.action) {
            NotificationManager.ACTION_OK -> 60 * 60 * 1000L // 1h
            NotificationManager.ACTION_NO -> 30 * 60 * 1000L // 30m
            else -> 0L
        }

        if (silenceDuration > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                prefs.updateLastNotified(System.currentTimeMillis() + silenceDuration)
            }
        }
    }
}
