package com.example.tasko.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasko.data.network.TipDto
import com.example.tasko.data.network.TipsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TipsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val tips: List<TipDto> = emptyList()
)

class TipsViewModel(
    private val repo: TipsRepository = TipsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(TipsUiState())
    val state: StateFlow<TipsUiState> = _state

    fun load() {
        if (_state.value.loading) return
        _state.value = _state.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val tips = repo.loadTips()
                _state.value = TipsUiState(loading = false, tips = tips)
            } catch (t: Throwable) {
                _state.value = TipsUiState(
                    loading = false,
                    error = t.message ?: "Network error"
                )
            }
        }
    }
}
