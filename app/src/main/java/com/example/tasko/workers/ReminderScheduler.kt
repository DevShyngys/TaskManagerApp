package com.example.tasko.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, taskId: Long, title: String, remindAt: Long) {
        val delayMs = remindAt - System.currentTimeMillis()
        if (delayMs <= 0) return

        val data = workDataOf(
            ReminderWorker.KEY_TASK_ID to taskId,
            ReminderWorker.KEY_TITLE to title
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag(taskId))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(taskId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(taskId))
    }

    private fun uniqueName(taskId: Long) = "reminder_$taskId"
    private fun tag(taskId: Long) = "reminder_tag_$taskId"
}
