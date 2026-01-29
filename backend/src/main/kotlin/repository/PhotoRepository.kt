package repository

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

    //private const val BASE_URL = "http://10.0.2.2/myvoronesh_api"
    private const val BASE_URL = "http://10.0.2.2/myvoronesh_api"


    // ✅ Абсолютный путь для СОХРАНЕНИЯ файлов
    private const val UPLOAD_BASE_DIR = "/opt/lampp/htdocs/myvoronesh_api"

    // ✅ Относительный путь для ЗАПИСИ в БД
    private const val RELATIVE_PHOTO_DIR = "uploads/photos"

    /**
     * Загрузить фото
     */
    fun uploadPhoto(
        userId: String,
        pointId: String,
        description: String?,
        fileBytes: ByteArray,
        fileName: String
    ): PhotoUploadResponse? = transaction {

        val photoId = UUID.randomUUID().toString()

        // Определяем расширение
        val extension = fileName.substringAfterLast(".", "jpg").lowercase()
        val allowedExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
        val finalExtension = if (extension in allowedExtensions) extension else "jpg"

        // ✅ Абсолютный путь для создания директории и сохранения файла
        val absoluteUserDir = File("$UPLOAD_BASE_DIR/$RELATIVE_PHOTO_DIR/$userId")
        if (!absoluteUserDir.exists()) {
            absoluteUserDir.mkdirs()
        }

        // Имя файла
        val photoFileName = "$photoId.$finalExtension"

        // ✅ Относительный путь для записи в БД
        val relativePath = "$RELATIVE_PHOTO_DIR/$userId/$photoFileName"

        // ✅ Абсолютный путь для сохранения файла
        val absolutePath = "$UPLOAD_BASE_DIR/$relativePath"
        val file = File(absolutePath)
        file.writeBytes(fileBytes)

        // ✅ В БД сохраняем ОТНОСИТЕЛЬНЫЙ путь
        UserPhotos.insert {
            it[id] = photoId
            it[UserPhotos.viserId] = userId
            it[UserPhotos.pointId] = pointId
            it[photoUrl] = relativePath  // ← Относительный!
            it[UserPhotos.description] = description
            it[createdAt] = LocalDateTime.now()
        }

        PhotoUploadResponse(
            id = photoId,
            photoUrl = relativePath,
            fullUrl = "$BASE_URL/$relativePath",
            description = description,
            pointId = pointId
        )
    }

    /**
     * Получить фото для точки
     */
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
                    fullUrl = "$BASE_URL/$photoUrl",
                    description = row[UserPhotos.description],
                    createdAt = row[UserPhotos.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }
    }

    /**
     * Получить все фото пользователя, сгруппированные по локациям
     */
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

        // Группируем по точкам
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
                        fullUrl = "$BASE_URL/${photo.photoUrl}",
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

    /**
     * Удалить фото
     */
    fun deletePhoto(userId: String, photoId: String): Boolean = transaction {
        // Получаем фото
        val photo = UserPhotos
            .select { (UserPhotos.id eq photoId) and (UserPhotos.viserId eq userId) }
            .singleOrNull() ?: return@transaction false

        // ✅ Удаляем файл по абсолютному пути
        val relativePath = photo[UserPhotos.photoUrl]
        val absolutePath = "$UPLOAD_BASE_DIR/$relativePath"
        val file = File(absolutePath)
        if (file.exists()) {
            file.delete()
        }

        // Удаляем из БД
        UserPhotos.deleteWhere { (id eq photoId) and (viserId eq userId) }
        true
    }

    /**
     * Подсчитать количество фото пользователя
     */
    fun countPhotos(userId: String): Int = transaction {
        UserPhotos
            .select { UserPhotos.viserId eq userId }
            .count()
            .toInt()
    }

    // Вспомогательный класс
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