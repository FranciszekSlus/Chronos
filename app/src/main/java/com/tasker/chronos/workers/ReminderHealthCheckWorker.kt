package com.tasker.chronos.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tasker.chronos.notifications.ReminderRescheduler
import java.util.concurrent.TimeUnit

/**
 * Okresowo odnawia alarmy przypomnien (AlarmManager moze je usunac
 * po optymalizacji baterii / killu procesu).
 */
class ReminderHealthCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val UNIQUE_WORK_NAME = "reminder_health_check"
        private const val TAG = "reminder_health_check"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ReminderHealthCheckWorker>(
                6, TimeUnit.HOURS
            )
                .addTag(TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )

            android.util.Log.d("ReminderHealthCheckWorker", "Zaplanowano health check (co 6h)")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("ReminderHealthCheckWorker", "Start health check...")
            ReminderRescheduler.rescheduleAll(applicationContext)
            android.util.Log.d("ReminderHealthCheckWorker", "Health check OK")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("ReminderHealthCheckWorker", "Blad: ${e.message}", e)
            Result.retry()
        }
    }
}
