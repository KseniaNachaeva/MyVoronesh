package com.example.myvoronesh.features.goals.mappers

import androidx.compose.ui.graphics.Color
import com.example.myvoronesh.data.remote.QuestProgressDto
import com.example.myvoronesh.features.goals.models.Goal

fun QuestProgressDto.toGoal(): Goal {
    return Goal(
        id = id,
        name = title,
        completed = completed,
        total = total,
        color = Color(
            android.graphics.Color.parseColor(color ?: "#CCCCCC")
        )
    )
}
