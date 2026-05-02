// Plik: ChronosApplication.kt
package com.tasker.chronos

import android.app.Application
import com.tasker.chronos.workers.HabitResetWorker
import com.tasker.chronos.workers.ReminderHealthCheckWorker


class ChronosApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("ChronosApplication", "🚀 Aplikacja startuje...")

        // ✅ WorkManager jest już automatycznie zainicjalizowany przez system
        // NIE TRZEBA go inicjalizować ręcznie!

        // ✅ Zaplanuj Workers
        initializeNotifications()
    }

    /**
     * Inicjalizuj wszystkie powiadomienia i zadania cykliczne
     */
    private fun initializeNotifications() {
        try {
            // ✅ Reset nawyków o północy
            HabitResetWorker.scheduleMidnightReset(this)
            ReminderHealthCheckWorker.schedule(this)



            android.util.Log.d("ChronosApplication", "✅ Powiadomienia zainicjalizowane")
        } catch (e: Exception) {
            android.util.Log.e("ChronosApplication", "❌ Błąd inicjalizacji: ${e.message}", e)
        }
    }
}