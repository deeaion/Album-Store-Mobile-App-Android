package com.albumstore.todo.ui.band

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albumstore.todo.data.band.Band
import com.albumstore.todo.data.band.BandRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BandUiState(
    val bands: List<Band> = emptyList(),
    val fetching: Boolean = false,
    val error: String? = null
)

class BandViewModel(
    private val bandRepository: BandRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BandUiState())
    val uiState: StateFlow<BandUiState> = _uiState

    fun fetchBands(online: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(fetching = true)
                val bands = bandRepository.fetchBands(online)
                Log.d("BandViewModel", "Fetched bands: $bands")
                _uiState.value = _uiState.value.copy(bands = bands, fetching = false)
            } catch (e: Exception) {
                Log.e("BandViewModel", "Error fetching bands: ${e.localizedMessage}")
                _uiState.value = _uiState.value.copy(fetching = false, error = e.localizedMessage)
            }
        }
    }


    companion object {
        fun Factory(bandRepository: BandRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(BandViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return BandViewModel(bandRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
