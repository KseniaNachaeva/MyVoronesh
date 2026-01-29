package routes

import config.Security
import dto.ApiResponse
import dto.LocationPhotosDto
import dto.MyPhotosResponse
import dto.PhotoDto
import dto.PhotoUploadResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import repository.PhotoRepository

fun Route.photoRoutes() {

    route("/photos") {

        /**
         * POST /photos
         * Загрузить фото (multipart/form-data)
         */
        post {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<PhotoUploadResponse>(success = false, message = "Unauthorized")
                )

            val multipart = call.receiveMultipart()

            var pointId: String? = null
            var description: String? = null
            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "point_id" -> pointId = part.value
                            "description" -> description = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name == "photo") {
                            fileName = part.originalFileName ?: "photo.jpg"
                            fileBytes = part.streamProvider().readBytes()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (pointId == null || fileBytes == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PhotoUploadResponse>(
                        success = false,
                        message = "point_id and photo are required"
                    )
                )
            }

            val result = PhotoRepository.uploadPhoto(
                userId = userId,
                pointId = pointId!!,
                description = description,
                fileBytes = fileBytes!!,
                fileName = fileName ?: "photo.jpg"
            )

            if (result != null) {
                call.respond(
                    ApiResponse(
                        success = true,
                        data = result,
                        message = "Фото загружено"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<PhotoUploadResponse>(success = false, message = "Failed to upload")
                )
            }
        }

        /**
         * GET /photos/point/{pointId}
         * Получить фото для точки
         */
        get("/point/{pointId}") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<List<PhotoDto>>(success = false, message = "Unauthorized")
                )

            val pointId = call.parameters["pointId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<List<PhotoDto>>(success = false, message = "pointId is required")
                )

            val photos = PhotoRepository.getPointPhotos(userId, pointId)

            call.respond(
                ApiResponse(
                    success = true,
                    data = photos
                )
            )
        }

        /**
         * GET /photos/my
         * Получить все мои фото
         */
        get("/my") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<MyPhotosResponse>(success = false, message = "Unauthorized")
                )

            val result = PhotoRepository.getMyPhotos(userId)

            call.respond(
                ApiResponse(
                    success = true,
                    data = result
                )
            )
        }

        /**
         * DELETE /photos/{photoId}
         * Удалить фото
         */
        delete("/{photoId}") {
            val token = call.request.headers["Authorization"]
                ?.removePrefix("Bearer ")
            val userId = Security.getUserId(token)
                ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Any>(success = false, message = "Unauthorized")
                )

            val photoId = call.parameters["photoId"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Any>(success = false, message = "photoId is required")
                )

            val deleted = PhotoRepository.deletePhoto(userId, photoId)

            if (deleted) {
                call.respond(
                    ApiResponse<Any>(success = true, message = "Фото удалено")
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Any>(success = false, message = "Фото не найдено")
                )
            }
        }
    }
}