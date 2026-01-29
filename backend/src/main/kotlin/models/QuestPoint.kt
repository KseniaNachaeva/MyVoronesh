package models

import java.math.BigDecimal

data class QuestPoint(
    val id: String,
    val questId: String,
    val title: String,
    val description: String,
    val shortDescription: String?,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val address: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val orderIndex: Int,
    val pointsReward: Int,
    val isActive: Boolean
)