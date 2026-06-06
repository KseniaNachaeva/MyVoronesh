package repository

import config.AppConfig
import dto.LocationPhotosDto
import dto.MyPhotosResponse
import dto.PhotoDto
import dto.PhotoUploadResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object UserPhotos : Table("user_photos") {
    val id = varchar("id", 36)
    val viserId = varchar("user_id", 36)
    val pointId = varchar("point_id", 36)
    val photoUrl = varchar("photo_url", 255)
    val thumbnailUrl = varchar("thumbnail_url", 255).nullable()
    val description = text("description").nullable()
    val isPublic = bool("is_public").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

object PhotoRepository {

    private const val RELATIVE_PHOTO_DIR = "photos"

    // uploadDir берётся из AppConfig, который инициализируется при старте приложения
    private val uploadDir get() = AppConfig.uploadDir
    private val baseUrl get() = AppConfig.baseUrl

    fun uploadPhoto(
        userId: String,
        pointId: String,
        description: String?,
        fileBytes: ByteArray,
        fileName: String
    ): PhotoUploadResponse? = transaction {
        val photoId = UUID.randomUUID().toString()

        val extension = fileName.substringAfterLast(".", "jpg").lowercase()
        val allowedExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
        val finalExtension = if (extension in allowedExtensions) extension else "jpg"

        // Папка: {uploadDir}/photos/{userId}/
        val absoluteUserDir = File("$uploadDir/$RELATIVE_PHOTO_DIR/$userId")
        if (!absoluteUserDir.exists()) {
            absoluteUserDir.mkdirs()
        }

        val photoFileName = "$photoId.$finalExtension"
        // В БД хранится относительный путь: photos/{userId}/{photoId}.jpg
        val relativePath = "$RELATIVE_PHOTO_DIR/$userId/$photoFileName"
        val absolutePath = "$uploadDir/$relativePath"

        File(absolutePath).writeBytes(fileBytes)

        UserPhotos.insert {
            it[id] = photoId
            it[UserPhotos.viserId] = userId
            it[UserPhotos.pointId] = pointId
            it[photoUrl] = relativePath
            it[UserPhotos.description] = description
            it[createdAt] = LocalDateTime.now()
        }

        PhotoUploadResponse(
            id = photoId,
            photoUrl = relativePath,
            fullUrl = "$baseUrl/uploads/$relativePath",
            description = description,
            pointId = pointId
        )
    }

    fun getPointPhotos(userId: String, pointId: String): List<PhotoDto> = transaction {
        UserPhotos
            .select {
                (UserPhotos.pointId eq pointId) and (UserPhotos.viserId eq userId)
            }
            .orderBy(UserPhotos.createdAt to SortOrder.DESC)
            .map { row: ResultRow ->
                val photoUrl = row[UserPhotos.photoUrl]
                PhotoDto(
                    id = row[UserPhotos.id],
                    photoUrl = photoUrl,
                    fullUrl = "$baseUrl/uploads/$photoUrl",
                    description = row[UserPhotos.description],
                    createdAt = row[UserPhotos.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }
    }

    fun getMyPhotos(userId: String): MyPhotosResponse = transaction {
        val photos = UserPhotos
            .innerJoin(QuestPoints, { pointId }, { QuestPoints.id })
            .innerJoin(Quests, { QuestPoints.questId }, { Quests.id })
            .select { UserPhotos.viserId eq userId }
            .orderBy(UserPhotos.createdAt to SortOrder.DESC)
            .map { row: ResultRow ->
                PhotoWithLocation(
                    id = row[UserPhotos.id],
                    photoUrl = row[UserPhotos.photoUrl],
                    description = row[UserPhotos.description],
                    createdAt = row[UserPhotos.createdAt],
                    pointId = row[QuestPoints.id],
                    locationName = row[QuestPoints.title],
                    questName = row[Quests.title]
                )
            }

        val grouped = photos.groupBy { it.pointId }
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val locationPhotos = grouped.map { (pointId, pointPhotos) ->
            val first = pointPhotos.first()
            LocationPhotosDto(
                pointId = pointId,
                locationName = first.locationName,
                questName = first.questName,
                date = first.createdAt.format(dateFormatter),
                photos = pointPhotos.map { photo ->
                    PhotoDto(
                        id = photo.id,
                        photoUrl = photo.photoUrl,
                        fullUrl = "$baseUrl/uploads/${photo.photoUrl}",
                        description = photo.description,
                        createdAt = photo.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                }
            )
        }

        MyPhotosResponse(
            data = locationPhotos,
            total = photos.size
        )
    }

    fun deletePhoto(userId: String, photoId: String): Boolean = transaction {
        val photo = UserPhotos
            .select { (UserPhotos.id eq photoId) and (UserPhotos.viserId eq userId) }
            .singleOrNull() ?: return@transaction false

        val relativePath = photo[UserPhotos.photoUrl]
        val file = File("$uploadDir/$relativePath")
        if (file.exists()) file.delete()

        UserPhotos.deleteWhere { (id eq photoId) and (viserId eq userId) }
        true
    }

    fun countPhotos(userId: String): Int = transaction {
        UserPhotos
            .select { UserPhotos.viserId eq userId }
            .count()
            .toInt()
    }

    private data class PhotoWithLocation(
        val id: String,
        val photoUrl: String,
        val description: String?,
        val createdAt: LocalDateTime,
        val pointId: String,
        val locationName: String,
        val questName: String
    )
}