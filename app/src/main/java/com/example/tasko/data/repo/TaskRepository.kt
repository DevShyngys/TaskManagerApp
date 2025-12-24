package com.example.tasko.data.repo

import com.example.tasko.data.db.TaskDao
import com.example.tasko.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    fun observeAll(): Flow<List<TaskEntity>> = dao.observeAll()
    suspend fun getById(id: Long): TaskEntity? = dao.getById(id)
    suspend fun upsert(task: TaskEntity): Long = dao.upsert(task)
    suspend fun delete(task: TaskEntity) = dao.delete(task)
    suspend fun setDone(id: Long, done: Boolean) = dao.setDone(id, done)
    suspend fun updatePin(id: Long, pinned: Boolean) = dao.updatePin(id, pinned)

}
