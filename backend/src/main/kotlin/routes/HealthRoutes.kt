package routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Route.healthRoute() {
    get("/health") {
        call.respond(mapOf("status" to "ok"))
    }
}
