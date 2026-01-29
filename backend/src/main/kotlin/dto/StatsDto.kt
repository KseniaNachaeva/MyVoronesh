package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsDto(
    @SerialName("completed_quests")
    val completedQuests: Int,
    @SerialName("total_quests")
    val totalQuests: Int,
    @SerialName("visited_points")
    val visitedPoints: Int,
    @SerialName("total_points")
    val totalPoints: Int,
    @SerialName("photos_count")
    val photosCount: Int,
    @SerialName("quests_progress")
    val questsProgress: List<QuestProgressDto>
)

@Serializable
data class QuestProgressDto(
    val id: String,
    val title: String,
    val color: String?,
    val total: Int,
    val completed: Int
)