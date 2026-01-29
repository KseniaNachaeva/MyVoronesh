package dto

import kotlinx.serialization.Serializable

// Универсальный ответ для операций без данных
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

// Для запроса включения квеста
@Serializable
data class SetQuestEnabledRequest(
    val enabled: Boolean
)


