package com.example.myvoronesh.features.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoronesh.data.remote.ApiClient
import com.example.myvoronesh.data.remote.LoginRequest
import com.example.myvoronesh.data.remote.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.google.gson.Gson
import com.example.myvoronesh.data.remote.AuthResponse


data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.login.isBlank()) {
            _uiState.value = state.copy(error = "Введите логин")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "Введите пароль")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = ApiClient.apiService.login(
                    LoginRequest(
                        login = state.login,
                        password = state.password
                    )
                )

                if (response.success && response.token != null) {
                    tokenManager.saveAuthData(
                        token = response.token,
                        userId = response.user?.id ?: response.userId ?: "",
                        userName = response.user?.name,
                        userLogin = response.user?.login,
                        email = response.user?.email
                    )
                    ApiClient.refreshClient()

                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message ?: "Ошибка входа"
                    )
                }
            }catch (e: HttpException) {

                                 val errorMessage = try {
                                         val errorBody = e.response()?.errorBody()?.string()
                                         if (errorBody != null) {
                                                 val gson = Gson()
                                                 val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)

                                                 errorResponse.message ?: "Ошибка входа: ${e.code()}"
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
                             } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка сети: ${e.message}"
                )
            }
        }
    }
}