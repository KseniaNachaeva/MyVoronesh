package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    val id: String,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("full_url")
    val fullUrl: String,
    val description: String?,
    @SerialName("created_at")
    val createdAt: String?
)

@Serializable
data class PhotoUploadResponse(
    val id: String,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("full_url")
    val fullUrl: String,
    val description: String?,
    @SerialName("point_id")
    val pointId: String
)

@Serializable
data class LocationPhotosDto(
    @SerialName("point_id")
    val pointId: String,
    @SerialName("location_name")
    val locationName: String,
    @SerialName("quest_name")
    val questName: String,
    val date: String,
    val photos: List<PhotoDto>
)

@Serializable
data class MyPhotosResponse(
    val data: List<LocationPhotosDto>,
    val total: Int
)