package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PointDto(
    val id: String,
    @SerialName("quest_id")
    val questId: String,
    val title: String,
    val description: String,
    @SerialName("short_description")
    val shortDescription: String? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("image_full_url")
    val imageFullUrl: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("order_index")
    val orderIndex: Int = 0,
    @SerialName("points_reward")
    val pointsReward: Int = 10,
    val visited: Int = 0,
    @SerialName("quest_title")
    val questTitle: String? = null,
    @SerialName("quest_color")
    val questColor: String? = null
)