package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarUploadResponse(
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("full_url")
    val fullUrl: String
)