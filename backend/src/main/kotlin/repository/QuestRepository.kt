package repository

import dto.QuestDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

// ==================== ТАБЛИЦЫ ====================

object Quests : Table("quests") {
    val id = varchar("id", 50)
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val imageUrl = varchar("image_url", 255).nullable()
    val iconUrl = varchar("icon_url", 255).nullable()
    val color = varchar("color", 7).default("#FFD54F")
    val difficulty = varchar("difficulty", 10).default("medium")
    val estimatedTimeMinutes = integer("estimated_time_minutes").default(60)
    val orderIndex = integer("order_index").default(0)
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

object UserQuestProgress : Table("user_quest_progress") {
    val userId = varchar("user_id", 36)
    val questId = varchar("quest_id", 50)
    val isSelected = bool("is_selected").default(false)
    val isEnabled = bool("is_enabled").default(false)
    val completedPoints = integer("completed_points").default(0)
    val totalPointsEarned = integer("total_points_earned").default(0)
    val startedAt = datetime("started_at").nullable()
    val completedAt = datetime("completed_at").nullable()
    val lastActivityAt = datetime("last_activity_at").nullable()

    override val primaryKey = PrimaryKey(userId, questId)
}

// ==================== РЕПОЗИТОРИЙ ====================

object QuestRepository {

    /**
     * Получить все активные квесты
     */
    fun getAllQuests(userId: String?): List<QuestDto> = transaction {
        val allQuests = Quests
            .select { Quests.isActive eq true }
            .orderBy(Quests.orderIndex)
            .toList()

        val userProgress = if (userId != null) {
            UserQuestProgress
                .select { UserQuestProgress.userId eq userId }
                .associate {
                    it[UserQuestProgress.questId] to Triple(
                        it[UserQuestProgress.isSelected],
                        it[UserQuestProgress.isEnabled],
                        it[UserQuestProgress.completedPoints]
                    )
                }
        } else {
            emptyMap()
        }

        allQuests.map { row ->
            val questId = row[Quests.id]
            val progress = userProgress[questId]

            val totalPoints = QuestPoints
                .select { (QuestPoints.questId eq questId) and (QuestPoints.isActive eq true) }
                .count().toInt()

            QuestDto(
                id = questId,
                title = row[Quests.title],
                description = row[Quests.description],
                imageUrl = row[Quests.imageUrl],
                iconUrl = row[Quests.iconUrl],
                color = row[Quests.color],
                difficulty = row[Quests.difficulty],
                estimatedTimeMinutes = row[Quests.estimatedTimeMinutes],
                orderIndex = row[Quests.orderIndex],
                // ⚠️ Конвертируем Boolean → Int (0/1)
                isSelected = if (progress?.first == true) 1 else 0,
                isEnabled = if (progress?.second == true) 1 else 0,
                totalPoints = totalPoints,
                completedPoints = progress?.third ?: 0
            )
        }
    }

    /**
     * Получить выбранные квесты пользователя
     */
    fun getSelectedQuests(userId: String): List<QuestDto> = transaction {
        Quests
            .innerJoin(UserQuestProgress, { Quests.id }, { questId })
            .select {
                (UserQuestProgress.userId eq userId) and
                        (UserQuestProgress.isSelected eq true)
            }
            .orderBy(Quests.orderIndex)
            .map { row ->
                val questId = row[Quests.id]

                val totalPoints = QuestPoints
                    .select { (QuestPoints.questId eq questId) and (QuestPoints.isActive eq true) }
                    .count().toInt()

                QuestDto(
                    id = questId,
                    title = row[Quests.title],
                    description = row[Quests.description],
                    imageUrl = row[Quests.imageUrl],
                    iconUrl = row[Quests.iconUrl],
                    color = row[Quests.color],
                    difficulty = row[Quests.difficulty],
                    estimatedTimeMinutes = row[Quests.estimatedTimeMinutes],
                    orderIndex = row[Quests.orderIndex],
                    isSelected = 1, // Всегда 1, т.к. это выбранные квесты
                    isEnabled = if (row[UserQuestProgress.isEnabled]) 1 else 0,
                    totalPoints = totalPoints,
                    completedPoints = row[UserQuestProgress.completedPoints]
                )
            }
    }

    /**
     * Переключить выбор квеста
     * @return true если успешно
     */
    fun toggleQuest(userId: String, questId: String): Boolean = transaction {
        val existing = UserQuestProgress
            .select {
                (UserQuestProgress.userId eq userId) and
                        (UserQuestProgress.questId eq questId)
            }
            .singleOrNull()

        if (existing != null) {
            val newValue = !existing[UserQuestProgress.isSelected]
            UserQuestProgress.update({
                (UserQuestProgress.userId eq userId) and
                        (UserQuestProgress.questId eq questId)
            }) {
                it[isSelected] = newValue
                it[isEnabled] = false
            }
        } else {
            UserQuestProgress.insert {
                it[UserQuestProgress.userId] = userId
                it[UserQuestProgress.questId] = questId
                it[isSelected] = true
                it[isEnabled] = false
                it[completedPoints] = 0
                it[startedAt] = LocalDateTime.now()
            }
        }
        true
    }

    /**
     * Включить/выключить отображение квеста на карте
     */
    fun setQuestEnabled(userId: String, questId: String, enabled: Boolean): Boolean = transaction {
        val updated = UserQuestProgress.update({
            (UserQuestProgress.userId eq userId) and
                    (UserQuestProgress.questId eq questId)
        }) {
            it[isEnabled] = enabled
        }
        updated > 0
    }
}