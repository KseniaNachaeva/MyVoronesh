package models

data class UserAuth(
    val id: String,
    val login: String,
    val passwordHash: String,
    val name: String
)
