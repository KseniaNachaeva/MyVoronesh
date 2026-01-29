import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import routes.*

fun Application.configureRouting() {
    routing {
        healthRoute()
        authRoutes()
        profileRoutes()
        questRoutes()
        pointRoutes()
        photoRoutes()
        statsRoutes()
    }
}

fun Route.healthRoute() {
    get("/health") {
        call.respond(mapOf("status" to "ok", "time" to System.currentTimeMillis()))
    }
}

