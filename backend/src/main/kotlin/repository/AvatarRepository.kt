package repository

import dto.AvatarUploadResponse
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File

object AvatarRepository {

    private const val BASE_URL = "http://10.0.2.2/myvoronesh_api"

    // ✅ Абсолютный путь для сохранения
    private const val UPLOAD_BASE_DIR = "/opt/lampp/htdocs/myvoronesh_api"

    // ✅ Относительный путь для БД
    private const val RELATIVE_AVATAR_DIR = "uploads/avatars"

    /**
     * Загрузить аватарку
     */
    fun uploadAvatar(
        userId: String,
        fileBytes: ByteArray,
        fileName: String
    ): AvatarUploadResponse? = transaction {

        // Определяем расширение
        val extension = fileName.substringAfterLast(".", "jpg").lowercase()
        val allowedExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
        val finalExtension = if (extension in allowedExtensions) extension else "jpg"

        // Удаляем старую аватарку
        val oldAvatarUrl = Users
            .slice(Users.avatarUrl)
            .select { Users.id eq userId }
            .singleOrNull()
            ?.get(Users.avatarUrl)

        if (oldAvatarUrl != null) {
            // ✅ Удаляем по абсолютному пути
            val oldAbsolutePath = "$UPLOAD_BASE_DIR/$oldAvatarUrl"
            val oldFile = File(oldAbsolutePath)
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }

        // ✅ Создаём директорию по абсолютному пути
        val absoluteDir = File("$UPLOAD_BASE_DIR/$RELATIVE_AVATAR_DIR")
        if (!absoluteDir.exists()) {
            absoluteDir.mkdirs()
        }

        // Имя файла
        val avatarFileName = "$userId.$finalExtension"

        // ✅ Относительный путь для БД
        val relativePath = "$RELATIVE_AVATAR_DIR/$avatarFileName"

        // ✅ Абсолютный путь для сохранения
        val absolutePath = "$UPLOAD_BASE_DIR/$relativePath"
        val file = File(absolutePath)
        file.writeBytes(fileBytes)

        // ✅ В БД сохраняем относительный путь
        Users.update({ Users.id eq userId }) {
            it[avatarUrl] = relativePath
        }

        val timestamp = System.currentTimeMillis()
        AvatarUploadResponse(
            avatarUrl = relativePath,
            fullUrl = "$BASE_URL/$relativePath?t=$timestamp"
        )
    }

    /**
     * Удалить аватарку
     */
    fun deleteAvatar(userId: String): Boolean = transaction {
        // Получаем текущую аватарку
        val avatarUrl = Users
            .slice(Users.avatarUrl)
            .select { Users.id eq userId }
            .singleOrNull()
            ?.get(Users.avatarUrl)

        if (avatarUrl != null) {
            // ✅ Удаляем по абсолютному пути
            val absolutePath = "$UPLOAD_BASE_DIR/$avatarUrl"
            val file = File(absolutePath)
            if (file.exists()) {
                file.delete()
            }
        }

        // Обнуляем в БД
        Users.update({ Users.id eq userId }) {
            it[Users.avatarUrl] = null
        }

        true
    }
}