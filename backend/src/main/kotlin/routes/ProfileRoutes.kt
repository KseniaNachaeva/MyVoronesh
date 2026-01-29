package routes

import config.Security
import dto.ApiResponse
import dto.AvatarUploadResponse
import dto.UpdateProfileRequest
import dto.UserDto
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.AvatarRepository
import repository.UserRepository

fun Route.profileRoutes() {

    route("/profile") {

        // Получить профиль
        get {
            val userId = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
                ?.let { Security.getUserId(it) }
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val user = UserRepository.findById(userId)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(user)
        }

        // Обновить профиль
        put {
            val userId = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
                ?.let { Security.getUserId(it) }
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val body = call.receive<UpdateProfileRequest>()

            val updated = UserRepository.updateProfile(
                userId = userId,
                name = body.name,
                email = body.email,
                birthDate = body.birthDate
            ) ?: return@put call.respond(HttpStatusCode.InternalServerError)

            call.respond(updated)
        }

        // ===== Аватарка =====

        /**
         * POST /profile/avatar
         * Загрузить аватарку
         */
        post("/avatar") {
            val userId = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
                ?.let { Security.getUserId(it) }
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<AvatarUploadResponse>(success = false, message = "Unauthorized")
                )

            val multipart = call.receiveMultipart()

            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "avatar") {
                            fileName = part.originalFileName ?: "avatar.jpg"
                            fileBytes = part.streamProvider().readBytes()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AvatarUploadResponse>(success = false, message = "Avatar file is required")
                )
            }

            val result = AvatarRepository.uploadAvatar(
                userId = userId,
                fileBytes = fileBytes!!,
                fileName = fileName ?: "avatar.jpg"
            )

            if (result != null) {
                call.respond(
                    ApiResponse(
                        success = true,
                        data = result,
                        message = "Аватарка обновлена"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<AvatarUploadResponse>(success = false, message = "Failed to upload")
                )
            }
        }

        /**
         * DELETE /profile/avatar
         * Удалить аватарку
         */
        delete("/avatar") {
            val userId = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
                ?.let { Security.getUserId(it) }
                ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Any>(success = false, message = "Unauthorized")
                )

            AvatarRepository.deleteAvatar(userId)

            call.respond(
                ApiResponse<Any>(success = true, message = "Аватарка удалена")
            )
        }
    }
}

