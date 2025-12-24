package com.example.tasko.data

import android.content.Context
import androidx.room.Room
import com.example.tasko.data.db.AppDatabase
import com.example.tasko.data.db.MIGRATION_1_2
import com.example.tasko.data.repo.TaskRepository

class AppContainer(context: Context) {
    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "tasko.db"
    )
        .addMigrations(MIGRATION_1_2)
        .build()


    val taskRepository = TaskRepository(db.taskDao())
}
