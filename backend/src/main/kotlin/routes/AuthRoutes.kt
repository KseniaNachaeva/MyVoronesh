package routes

import config.Security
import dto.AuthResponse
import dto.LoginRequest
import dto.RegisterRequest
import dto.UserDto

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

import repository.UserRepository
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {

    route("/auth") {

        post("/login") {
            val request = call.receive<LoginRequest>()

            val auth = UserRepository.findAuthByLogin(request.login)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthResponse(false, message = "Неверный логин или пароль")
                )

            val passwordOk = try {
                BCrypt.checkpw(request.password, auth.passwordHash)
            } catch (e: Exception) {
                false
            }

            if (!passwordOk) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthResponse(success = false, message = "Неверный логин или пароль")
                )
            }

            val user = UserRepository.findById(auth.id)
                ?: return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    AuthResponse(false, message = "User not found")
                )

            // ✅ Используем Security.generateToken для нового формата
            // Но для совместимости можно оставить старый формат:
            val token = "test-token-${user.id}"

            // Или новый формат (раскомментируйте когда будете готовы):
            // val token = Security.generateToken(user.id)

            call.respond(
                AuthResponse(
                    success = true,
                    token = token,
                    user = user
                )
            )
        }

        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.password.length < 6) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse(success = false, message = "Пароль должен быть не менее 6 символов")
                )
            }

            val exists = UserRepository.findAuthByLogin(request.login)
            if (exists != null) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    AuthResponse(false, message = "Логин уже занят")
                )
            }

            val user = UserRepository.createUser(request.login, request.password)
                ?: return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    AuthResponse(false, message = "Ошибка создания пользователя")
                )

            val token = "test-token-${user.id}"

            call.respond(
                AuthResponse(
                    success = true,
                    token = token,
                    user = user
                )
            )
        }
    }
}
