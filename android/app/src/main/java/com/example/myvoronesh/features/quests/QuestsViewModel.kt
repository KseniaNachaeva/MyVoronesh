package com.example.myvoronesh.features.quests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoronesh.features.quests.models.Quest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestsViewModel(application: Application) : AndroidViewModel(application) {

    val allQuests: StateFlow<List<Quest>> = QuestsRepository.allQuests
    val selectedQuestIds: StateFlow<Set<String>> = QuestsRepository.selectedQuestIds

    val questsForDialog: StateFlow<List<Quest>> = combine(allQuests, selectedQuestIds) { quests, selectedIds ->
        quests.filter { selectedIds.contains(it.id) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        QuestsRepository.init(application)
        loadQuests()
    }

    private fun loadQuests() {
        viewModelScope.launch {
            QuestsRepository.loadQuests()
        }
    }

    fun toggleQuestSelection(questId: String) {
        viewModelScope.launch {
            QuestsRepository.toggleQuestSelection(questId)
        }
    }

    fun isQuestSelected(questId: String): Boolean {
        return QuestsRepository.isQuestSelected(questId)
    }

    fun toggleQuestInDialog(questId: String, enabled: Boolean) {
        viewModelScope.launch {
            QuestsRepository.setQuestEnabled(questId, enabled)
        }
    }
}