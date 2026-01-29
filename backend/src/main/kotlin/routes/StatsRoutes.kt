package routes

import config.Security
import dto.ApiResponse
import dto.StatsDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.StatsRepository

fun Route.statsRoutes() {

    route("/stats") {

        /**
         * GET /stats
         * Получить статистику пользователя
         */
        get {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<StatsDto>(success = false, message = "Unauthorized")
                )

            val stats = StatsRepository.getStats(userId)

            call.respond(
                ApiResponse(
                    success = true,
                    data = stats
                )
            )
        }
    }
}