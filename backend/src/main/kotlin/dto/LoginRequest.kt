package dto

import kotlinx.serialization.Serializable


data class LoginRequest(
    val login: String,
    val password: String
)

