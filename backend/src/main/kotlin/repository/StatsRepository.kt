package repository

import dto.QuestProgressDto
import dto.StatsDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object StatsRepository {

    fun getStats(userId: String): StatsDto = transaction {

        // 1. Всего активных квестов
        val totalQuests = Quests
            .select { Quests.isActive eq true }
            .count()
            .toInt()

        // 2. Завершённые квесты (где посещены все точки)
        val completedQuests = countCompletedQuests(userId)

        // 3. Посещённые точки
        val visitedPoints = UserPointProgress
            .select {
                (UserPointProgress.userId eq userId) and
                        (UserPointProgress.visited eq true)
            }
            .count()
            .toInt()

        // 4. Всего активных точек
        val totalPoints = QuestPoints
            .select { QuestPoints.isActive eq true }
            .count()
            .toInt()

        // 5. Количество фото
        val photosCount = UserPhotos
            .select { UserPhotos.viserId eq userId }
            .count()
            .toInt()

        // 6. Прогресс по каждому квесту
        val questsProgress = getQuestsProgress(userId)

        StatsDto(
            completedQuests = completedQuests,
            totalQuests = totalQuests,
            visitedPoints = visitedPoints,
            totalPoints = totalPoints,
            photosCount = photosCount,
            questsProgress = questsProgress
        )
    }

    private fun countCompletedQuests(userId: String): Int {
        // Получаем все квесты с их точками
        val questPointCounts = QuestPoints
            .slice(QuestPoints.questId, QuestPoints.id.count())
            .select { QuestPoints.isActive eq true }
            .groupBy(QuestPoints.questId)
            .associate {
                it[QuestPoints.questId] to it[QuestPoints.id.count()].toInt()
            }

        // Получаем посещённые точки по квестам
        val visitedByQuest = UserPointProgress
            .innerJoin(QuestPoints, { pointId }, { QuestPoints.id })
            .slice(QuestPoints.questId, UserPointProgress.pointId.count())
            .select {
                (UserPointProgress.userId eq userId) and
                        (UserPointProgress.visited eq true)
            }
            .groupBy(QuestPoints.questId)
            .associate {
                it[QuestPoints.questId] to it[UserPointProgress.pointId.count()].toInt()
            }

        // Считаем завершённые (где visited == total)
        return questPointCounts.count { (questId, total) ->
            val visited = visitedByQuest[questId] ?: 0
            total > 0 && visited == total
        }
    }

    private fun getQuestsProgress(userId: String): List<QuestProgressDto> {
        // Получаем все активные квесты
        val quests = Quests
            .select { Quests.isActive eq true }
            .orderBy(Quests.orderIndex to SortOrder.ASC)
            .map { row: ResultRow ->
                Triple(
                    row[Quests.id],
                    row[Quests.title],
                    row[Quests.color]
                )
            }

        // Считаем точки по квестам
        val totalByQuest = QuestPoints
            .slice(QuestPoints.questId, QuestPoints.id.count())
            .select { QuestPoints.isActive eq true }
            .groupBy(QuestPoints.questId)
            .associate {
                it[QuestPoints.questId] to it[QuestPoints.id.count()].toInt()
            }

        // Считаем посещённые точки
        val completedByQuest = UserPointProgress
            .innerJoin(QuestPoints, { pointId }, { QuestPoints.id })
            .slice(QuestPoints.questId, UserPointProgress.pointId.count())
            .select {
                (UserPointProgress.userId eq userId) and
                        (UserPointProgress.visited eq true)
            }
            .groupBy(QuestPoints.questId)
            .associate {
                it[QuestPoints.questId] to it[UserPointProgress.pointId.count()].toInt()
            }

        return quests.map { (id, title, color) ->
            QuestProgressDto(
                id = id,
                title = title,
                color = color,
                total = totalByQuest[id] ?: 0,
                completed = completedByQuest[id] ?: 0
            )
        }
    }
}