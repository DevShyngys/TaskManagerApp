package com.example.tasko.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasko.data.db.TaskEntity
import com.example.tasko.data.repo.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, ACTIVE, DONE }

data class UiState(
    val tasks: List<TaskEntity> = emptyList(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL
)

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)

    val uiState: StateFlow<UiState> =
        combine(repo.observeAll(), query, filter) { tasks, q, f ->
            val filtered = tasks
                .filter { it.title.contains(q, ignoreCase = true) }
                .filter {
                    when (f) {
                        TaskFilter.ALL -> true
                        TaskFilter.ACTIVE -> !it.isDone
                        TaskFilter.DONE -> it.isDone
                    }
                }
            UiState(filtered, q, f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setQuery(q: String) { query.value = q }
    fun setFilter(f: TaskFilter) { filter.value = f }

    fun toggleDone(id: Long, done: Boolean) {
        viewModelScope.launch { repo.setDone(id, done) }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch { repo.delete(task) }
    }

    fun save(task: TaskEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.upsert(task)
            onSaved(id)
        }
    }


    suspend fun getById(id: Long): TaskEntity? {
        return repo.getById(id)
    }

    fun togglePin(id: Long, pinned: Boolean) = viewModelScope.launch {
        repo.updatePin(id, pinned)
    }


}
