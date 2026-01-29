package routes

import config.Security
import dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.QuestRepository

fun Route.questRoutes() {

    route("/quests") {

        /**
         * GET /quests
         * Возвращает List<QuestDto> напрямую (не обёрнутый в ApiResponse)
         */
        get {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)

            val quests = QuestRepository.getAllQuests(userId)

            // ⚠️ Возвращаем список напрямую!
            call.respond(quests)
        }

        /**
         * GET /quests/selected
         * Возвращает List<QuestDto> напрямую
         */
        get("/selected") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@get call.respond(HttpStatusCode.Unauthorized, emptyList<QuestDto>())

            val quests = QuestRepository.getSelectedQuests(userId)

            // ⚠️ Возвращаем список напрямую!
            call.respond(quests)
        }

        /**
         * POST /quests/{questId}/toggle
         * Переключает выбор квеста
         * Возвращает ApiResponse<Unit>
         */
        post("/{questId}/toggle") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(success = false, message = "Unauthorized")
                )

            val questId = call.parameters["questId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(success = false, message = "questId is required")
                )

            val success = QuestRepository.toggleQuest(userId, questId)

            call.respond(ApiResponse<Unit>(success = success))
        }

        /**
         * PATCH /quests/{questId}/enabled
         * Включает/выключает квест на карте
         * Body: { "enabled": true/false }
         * Возвращает ApiResponse<Unit>
         */
        patch("/{questId}/enabled") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@patch call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(success = false, message = "Unauthorized")
                )

            val questId = call.parameters["questId"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(success = false, message = "questId is required")
                )

            val request = call.receive<SetQuestEnabledRequest>()
            val success = QuestRepository.setQuestEnabled(userId, questId, request.enabled)

            call.respond(ApiResponse<Unit>(success = success))
        }
    }
}