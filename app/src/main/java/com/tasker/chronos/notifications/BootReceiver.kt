// Plik: notifications/BootReceiver.kt
package com.tasker.chronos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            "android.intent.action.QUICKBOOT_POWERON"
        )
        if (intent.action !in supportedActions) return

        android.util.Log.d("BootReceiver", "📱 System event detected - refreshing reminders")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderRescheduler.rescheduleAll(context)
                android.util.Log.d("BootReceiver", "✅ Reminder refresh complete")
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "❌ Błąd przywracania: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}