package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val login: String,
    val name: String,
    val email: String? = null,
    val birthDate: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("avatar_full_url")
    val avatarFullUrl: String? = null
)



