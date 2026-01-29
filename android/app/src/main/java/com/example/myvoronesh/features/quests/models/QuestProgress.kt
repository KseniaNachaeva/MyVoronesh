package com.example.myvoronesh.features.quests.models

import androidx.compose.ui.graphics.Color

data class QuestProgress(
    val name: String,
    val completed: Int,
    val total: Int,
    val color: Color
)