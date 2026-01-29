package models

data class Quest(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val iconUrl: String?,
    val color: String,
    val difficulty: String,
    val estimatedTimeMinutes: Int,
    val orderIndex: Int,
    val isActive: Boolean
)