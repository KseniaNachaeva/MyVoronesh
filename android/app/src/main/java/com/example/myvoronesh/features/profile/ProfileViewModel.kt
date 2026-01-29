package com.example.myvoronesh.features.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoronesh.data.remote.ApiClient
import com.example.myvoronesh.data.remote.TokenManager
import com.example.myvoronesh.data.remote.UpdateProfileRequest
import com.example.myvoronesh.features.profile.models.Profile
import com.example.myvoronesh.profile.utils.normalizeBirthDateFromApi
import com.example.myvoronesh.profile.utils.normalizeBirthDateToApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

data class ProfileUiState(
    val profile: Profile = Profile(),
    val showEditDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val tokenManager = TokenManager.getInstance(application)
    private val context = application.applicationContext

    init {
        ApiClient.init(application)
        loadProfile()
    }

    // =========================
// ЗАГРУЗКА ПРОФИЛЯ
// =========================
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // 🔹 Ktor возвращает UserDto напрямую
                val user = ApiClient.apiService.getProfile()

                _uiState.value = _uiState.value.copy(
                    profile = Profile(
                        name = user.name,
                        birthDate = normalizeBirthDateFromApi(user.birthDate ?: ""),
                        email = user.email ?: "",
                        avatarUrl = user.avatarUrl,
                        avatarFullUrl = user.avatarFullUrl
                    ),
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки профиля: ${e.message}"
                )
            }
        }
    }

    // =========================
// ЗАГРУЗКА АВАТАРКИ
// (пока оставляем как есть)
// =========================
    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingAvatar = true,
                error = null
            )

            try {
                val tempFile = createTempFileFromUri(imageUri)
                    ?: run {
                        _uiState.value = _uiState.value.copy(
                            isUploadingAvatar = false,
                            error = "Не удалось прочитать файл"
                        )
                        return@launch
                    }

                val requestFile =
                    tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val avatarPart =
                    MultipartBody.Part.createFormData("avatar", tempFile.name, requestFile)

                val response = ApiClient.apiService.uploadAvatar(
                    avatar = avatarPart
                )

                tempFile.delete()

                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        profile = _uiState.value.profile.copy(
                            avatarUrl = response.data.avatarUrl,
                            avatarFullUrl = response.data.fullUrl
                        ),
                        isUploadingAvatar = false,
                        successMessage = "Аватарка обновлена!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUploadingAvatar = false,
                        error = response.message ?: "Ошибка загрузки аватарки"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val mimeType = context.contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val tempFile = File.createTempFile("avatar_", ".$extension", context.cacheDir)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    // =========================
// РЕДАКТИРОВАНИЕ ПРОФИЛЯ
// =========================
    fun updateProfile(name: String, birthDate: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                successMessage = null
            )

            try {
                val updatedUser = ApiClient.apiService.updateProfile(
                    UpdateProfileRequest(
                        name = name.trim(),
                        email = email.trim().ifBlank { null },
                        birthDate = birthDate
                            .trim()
                            .ifBlank { null }
                            ?.let { normalizeBirthDateToApi(it) } // dd.MM.yyyy → yyyy-MM-dd
                    )
                )

                // ✅ success НЕ НУЖЕН — если пришёл UserDto, значит всё ок
                _uiState.value = _uiState.value.copy(
                    profile = _uiState.value.profile.copy(
                        name = updatedUser.name,
                        birthDate = normalizeBirthDateFromApi(updatedUser.birthDate ?: ""),
                        email = updatedUser.email ?: ""
                    ),
                    showEditDialog = false,
                    isSaving = false,
                    successMessage = "Профиль сохранён!"
                )

                // Обновляем имя в токене
                tokenManager.userName = updatedUser.name

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }




    // =========================
// UI-ВСПОМОГАТЕЛЬНОЕ
// =========================
    fun showEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = true)
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun logout(onLogout: () -> Unit) {
        tokenManager.clear()
        onLogout()
    }
}
