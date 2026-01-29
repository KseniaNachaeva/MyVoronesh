package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val color: String? = "#FFD54F",
    val difficulty: String = "medium",
    @SerialName("estimated_time_minutes")
    val estimatedTimeMinutes: Int = 60,
    @SerialName("order_index")
    val orderIndex: Int = 0,

    // ⚠️ Int вместо Boolean для совместимости с Android!
    @SerialName("is_selected")
    val isSelected: Int = 0,
    @SerialName("is_enabled")
    val isEnabled: Int = 0,
    @SerialName("total_points")
    val totalPoints: Int = 0,
    @SerialName("completed_points")
    val completedPoints: Int = 0
)
