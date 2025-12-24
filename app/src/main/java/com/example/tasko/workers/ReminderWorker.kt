package com.example.tasko.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tasko.MainActivity
import com.example.tasko.TaskoApp
import com.example.tasko.R
import kotlin.random.Random
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


class ReminderWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Task reminder"
        val taskId = inputData.getLong(KEY_TASK_ID, -1)

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            taskId.toInt().coerceAtLeast(0),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, TaskoApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(appContext)
        val hasPermission =
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    appContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true

        if (!hasPermission) {
            return Result.success()
        }

        return try {
            NotificationManagerCompat.from(appContext).notify(Random.nextInt(), notification)
            Result.success()
        } catch (e: SecurityException) {
            Result.success()
        }


        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "key_title"
        const val KEY_TASK_ID = "key_task_id"
    }
}
