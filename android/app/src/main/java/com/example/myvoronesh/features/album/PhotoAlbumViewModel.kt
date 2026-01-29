package com.example.myvoronesh.features.album

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myvoronesh.features.album.models.LocationPhotos
import com.example.myvoronesh.features.album.models.Photo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PhotoAlbumUiState(
    val photosByLocation: List<LocationPhotos> = emptyList(),
    val totalPhotos: Int = 0,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class PhotoAlbumViewModel(application: Application) :
    AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            delay(16)
            loadPhotos()
        }
    }


    private val repository = PhotoRepository(application)

    private val _uiState = MutableStateFlow(PhotoAlbumUiState())
    val uiState: StateFlow<PhotoAlbumUiState> = _uiState.asStateFlow()

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getMyPhotos()
                .onSuccess { dtoList ->
                    val locations = dtoList.map { dto ->
                        LocationPhotos(
                            pointId = dto.pointId,
                            locationName = dto.locationName,
                            questName = dto.questName,
                            date = dto.date,
                            photos = dto.photos.map { p ->
                                Photo(
                                    id = p.id,
                                    imageUrl = p.fullUrl
                                        .replace("http://localhost/", "http://10.0.2.2/")
                                        .replace("http://127.0.0.1/", "http://10.0.2.2/"),
                                    description = p.description ?: ""
                                )
                            }
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        photosByLocation = locations,
                        totalPhotos = locations.sumOf { it.photos.size },
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message
                    )
                }
        }
    }

    fun uploadPhoto(uri: Uri, pointId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)

            repository.uploadPhoto(uri, pointId)
                .onSuccess {
                    loadPhotos()
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        successMessage = "Фото загружено"
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        error = it.message
                    )
                }
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            repository.deletePhoto(photoId)
                .onSuccess {
                    loadPhotos()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Фото удалено"
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value =
            _uiState.value.copy(error = null, successMessage = null)
    }
}
