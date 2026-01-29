package routes

import config.Security
import dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.PointRepository

fun Route.pointRoutes() {

    route("/points") {

        /**
         * GET /points?questId=...
         * Возвращает List<PointDto> напрямую
         * ⚠️ Параметр называется questId (не quest_id)!
         */
        get {
            val questId = call.request.queryParameters["questId"]

            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)

            val points = PointRepository.getPoints(questId, userId)

            // ⚠️ Возвращаем список напрямую!
            call.respond(points)
        }

        /**
         * GET /points/visible
         * Возвращает List<PointDto> напрямую
         */
        get("/visible") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)

            if (userId == null) {
                call.respond(emptyList<PointDto>())
                return@get
            }

            val points = PointRepository.getVisiblePoints(userId)

            // ⚠️ Возвращаем список напрямую!
            call.respond(points)
        }

        /**
         * POST /points/{pointId}/visited/toggle
         * Переключает посещение точки
         * Возвращает ApiResponse<Unit>
         */
        post("/{pointId}/visited/toggle") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(success = false, message = "Unauthorized")
                )

            val pointId = call.parameters["pointId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(success = false, message = "pointId is required")
                )

            PointRepository.togglePointVisited(userId, pointId)

            call.respond(ApiResponse<Unit>(success = true))
        }
    }
}