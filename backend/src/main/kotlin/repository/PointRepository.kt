package repository

import dto.PointDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

// ==================== ТАБЛИЦЫ ====================

object QuestPoints : Table("quest_points") {
    val id = varchar("id", 36)
    val questId = varchar("quest_id", 50)
    val title = varchar("title", 150)
    val description = text("description")
    val shortDescription = varchar("short_description", 255).nullable()
    val latitude = decimal("latitude", 10, 8)
    val longitude = decimal("longitude", 11, 8)
    val address = varchar("address", 255).nullable()
    val imageUrl = varchar("image_url", 255).nullable()
    val audioUrl = varchar("audio_url", 255).nullable()
    val orderIndex = integer("order_index").default(0)
    val pointsReward = integer("points_reward").default(10)
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

object UserPointProgress : Table("user_point_progress") {
    val userId = varchar("user_id", 36)
    val pointId = varchar("point_id", 36)
    val visited = bool("visited").default(false)
    val visitedAt = datetime("visited_at").nullable()
    val rating = integer("rating").nullable()
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(userId, pointId)
}

// ==================== РЕПОЗИТОРИЙ ====================

object PointRepository {

    private const val BASE_URL = "http://10.0.2.2/myvoronesh_api"

    /**
     * Получить точки квеста
     */
    fun getPoints(questId: String?, userId: String?): List<PointDto> = transaction {
        val query = if (questId != null) {
            QuestPoints.select {
                (QuestPoints.questId eq questId) and (QuestPoints.isActive eq true)
            }
        } else {
            QuestPoints.select { QuestPoints.isActive eq true }
        }

        // Получаем прогресс пользователя
        val visitedPoints = if (userId != null) {
            UserPointProgress
                .select { UserPointProgress.userId eq userId }
                .associate { it[UserPointProgress.pointId] to it[UserPointProgress.visited] }
        } else {
            emptyMap()
        }

        // ✅ Исправленный orderBy
        query
            .orderBy(
                QuestPoints.questId to SortOrder.ASC,
                QuestPoints.orderIndex to SortOrder.ASC
            )
            .map { row: ResultRow ->  // ✅ Явно указываем тип
                row.toPointDto(visitedPoints[row[QuestPoints.id]] ?: false)
            }
    }

    /**
     * Получить видимые точки на карте
     */
    fun getVisiblePoints(userId: String): List<PointDto> = transaction {
        val enabledQuestIds = UserQuestProgress
            .slice(UserQuestProgress.questId)
            .select {
                (UserQuestProgress.userId eq userId) and
                        (UserQuestProgress.isEnabled eq true)
            }
            .map { it[UserQuestProgress.questId] }

        if (enabledQuestIds.isEmpty()) {
            return@transaction emptyList()
        }

        val visitedPoints = UserPointProgress
            .select { UserPointProgress.userId eq userId }
            .associate { it[UserPointProgress.pointId] to it[UserPointProgress.visited] }

        QuestPoints
            .innerJoin(Quests, { questId }, { Quests.id })
            .select {
                (QuestPoints.questId inList enabledQuestIds) and
                        (QuestPoints.isActive eq true)
            }
            // ✅ Исправленный orderBy
            .orderBy(QuestPoints.orderIndex to SortOrder.ASC)
            .map { row: ResultRow ->  // ✅ Явно указываем тип
                val pointId = row[QuestPoints.id]
                val imageUrl = row[QuestPoints.imageUrl]

                PointDto(
                    id = pointId,
                    questId = row[QuestPoints.questId],
                    title = row[QuestPoints.title],
                    description = row[QuestPoints.description],
                    shortDescription = row[QuestPoints.shortDescription],
                    latitude = row[QuestPoints.latitude].toDouble(),
                    longitude = row[QuestPoints.longitude].toDouble(),
                    address = row[QuestPoints.address],
                    imageUrl = imageUrl,
                    imageFullUrl = imageUrl?.let { "$BASE_URL/$it?t=${System.currentTimeMillis()}" },
                    audioUrl = row[QuestPoints.audioUrl],
                    orderIndex = row[QuestPoints.orderIndex],
                    pointsReward = row[QuestPoints.pointsReward],
                    visited = if (visitedPoints[pointId] == true) 1 else 0,
                    questTitle = row[Quests.title],
                    questColor = row[Quests.color]
                )
            }
    }

    /**
     * Переключить посещение точки
     */
    fun togglePointVisited(userId: String, pointId: String): Boolean = transaction {
        val existing = UserPointProgress
            .select {
                (UserPointProgress.userId eq userId) and
                        (UserPointProgress.pointId eq pointId)
            }
            .singleOrNull()

        val newVisited: Boolean

        if (existing != null) {
            newVisited = !existing[UserPointProgress.visited]
            UserPointProgress.update({
                (UserPointProgress.userId eq userId) and
                        (UserPointProgress.pointId eq pointId)
            }) {
                it[visited] = newVisited
                it[visitedAt] = if (newVisited) LocalDateTime.now() else null
            }
        } else {
            newVisited = true
            UserPointProgress.insert {
                it[UserPointProgress.userId] = userId
                it[UserPointProgress.pointId] = pointId
                it[visited] = true
                it[visitedAt] = LocalDateTime.now()
            }
        }

        updateQuestProgress(userId, pointId)
        newVisited
    }

    private fun updateQuestProgress(userId: String, pointId: String) {
        val questId = QuestPoints
            .slice(QuestPoints.questId)
            .select { QuestPoints.id eq pointId }
            .singleOrNull()
            ?.get(QuestPoints.questId) ?: return

        val completedCount = UserPointProgress
            .innerJoin(QuestPoints, { UserPointProgress.pointId }, { QuestPoints.id })
            .select {
                (UserPointProgress.userId eq userId) and
                        (QuestPoints.questId eq questId) and
                        (UserPointProgress.visited eq true)
            }
            .count().toInt()

        UserQuestProgress.update({
            (UserQuestProgress.userId eq userId) and
                    (UserQuestProgress.questId eq questId)
        }) {
            it[UserQuestProgress.completedPoints] = completedCount
            it[lastActivityAt] = LocalDateTime.now()
        }
    }

    // ✅ Extension функция вне transaction
    private fun ResultRow.toPointDto(isVisited: Boolean): PointDto {
        val imageUrl = this[QuestPoints.imageUrl]
        return PointDto(
            id = this[QuestPoints.id],
            questId = this[QuestPoints.questId],
            title = this[QuestPoints.title],
            description = this[QuestPoints.description],
            shortDescription = this[QuestPoints.shortDescription],
            latitude = this[QuestPoints.latitude].toDouble(),
            longitude = this[QuestPoints.longitude].toDouble(),
            address = this[QuestPoints.address],
            imageUrl = imageUrl,
            imageFullUrl = imageUrl?.let { "$BASE_URL/$it" },
            audioUrl = this[QuestPoints.audioUrl],
            orderIndex = this[QuestPoints.orderIndex],
            pointsReward = this[QuestPoints.pointsReward],
            visited = if (isVisited) 1 else 0
        )
    }
}