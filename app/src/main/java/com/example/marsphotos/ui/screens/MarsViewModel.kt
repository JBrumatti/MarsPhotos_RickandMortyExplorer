package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.MarsApi
import com.example.marsphotos.network.MarsPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed interface MarsUiState {
    data class Success(val photos: String, val randomPhoto: MarsPhoto) : MarsUiState
    data object Error : MarsUiState
    data object Loading : MarsUiState
}
class MarsViewModel : ViewModel() {
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set
    private var loading = false
    init { getMarsPhotos() }
    fun getMarsPhotos() {
        if (loading) return
        loading = true
        marsUiState = MarsUiState.Loading
        viewModelScope.launch {
            try {
                val photos = MarsApi.service.getPhotos()
                marsUiState = photos.randomOrNull()?.let {
                    MarsUiState.Success("${photos.size} fotografias de Marte recebidas", it)
                } ?: MarsUiState.Error
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { marsUiState = MarsUiState.Error
            } finally { loading = false }
        }
    }
}
