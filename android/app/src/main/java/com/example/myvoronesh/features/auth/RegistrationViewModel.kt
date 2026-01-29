package com.example.myvoronesh.features.auth

import android.app.Application
import retrofit2.HttpException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoronesh.data.remote.ApiClient
import com.example.myvoronesh.data.remote.RegisterRequest
import com.example.myvoronesh.data.remote.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.example.myvoronesh.data.remote.AuthResponse


data class RegistrationUiState(
    val login: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val tokenManager = TokenManager.getInstance(application)

    init {
        ApiClient.init(application)
    }

    fun updateLogin(value: String) {
        _uiState.value = _uiState.value.copy(login = value, error = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }


    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.login.isBlank()) {
            _uiState.value = state.copy(error = "Введите логин")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Пароль должен быть не менее 6 символов")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = ApiClient.apiService.register(
                    RegisterRequest(
                        login = state.login,
                        password = state.password
                    )
                )

                if (response.success && response.token != null) {
                    tokenManager.saveAuthData(
                        token = response.token,
                        userId = response.user?.id ?: response.userId ?: "",
                        userName = response.user?.name,
                        userLogin = response.user?.login
                    )
                    ApiClient.refreshClient()

                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }  else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message ?: "Ошибка регистрации"
                    )
                }
            } catch (e: HttpException) {

                val errorMessage = try {
                    val errorBody = e.response()?.errorBody()?.string()
                    if (errorBody != null) {

                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)
                        errorResponse.message ?: "Ошибка: ${e.code()}"
                    } else {
                        "Ошибка сервера: ${e.code()}"
                    }
                } catch (parseEx: Exception) {
                    "Ошибка сервера: ${e.code()}"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка сети: ${e.message}"
                )
            }
        }
    }
}