package repository

import config.AppConfig
import dto.AvatarUploadResponse
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File

object AvatarRepository {

    private const val RELATIVE_AVATAR_DIR = "avatars"

    private val uploadDir get() = AppConfig.uploadDir
    private val baseUrl get() = AppConfig.baseUrl

    fun uploadAvatar(
        userId: String,
        fileBytes: ByteArray,
        fileName: String
    ): AvatarUploadResponse? = transaction {
        val extension = fileName.substringAfterLast(".", "jpg").lowercase()
        val allowedExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
        val finalExtension = if (extension in allowedExtensions) extension else "jpg"

        // Удалить старый аватар с диска
        val oldAvatarUrl = Users
            .slice(Users.avatarUrl)
            .select { Users.id eq userId }
            .singleOrNull()
            ?.get(Users.avatarUrl)

        if (oldAvatarUrl != null) {
            val oldFile = File("$uploadDir/$oldAvatarUrl")
            if (oldFile.exists()) oldFile.delete()
        }

        // Папка: {uploadDir}/avatars/
        val absoluteDir = File("$uploadDir/$RELATIVE_AVATAR_DIR")
        if (!absoluteDir.exists()) absoluteDir.mkdirs()

        val avatarFileName = "$userId.$finalExtension"
        // В БД: avatars/{userId}.jpg
        val relativePath = "$RELATIVE_AVATAR_DIR/$avatarFileName"
        val absolutePath = "$uploadDir/$relativePath"

        File(absolutePath).writeBytes(fileBytes)

        Users.update({ Users.id eq userId }) {
            it[avatarUrl] = relativePath
        }

        val timestamp = System.currentTimeMillis()
        AvatarUploadResponse(
            avatarUrl = relativePath,
            fullUrl = "$baseUrl/uploads/$relativePath?t=$timestamp"
        )
    }

    fun deleteAvatar(userId: String): Boolean = transaction {
        val avatarUrl = Users
            .slice(Users.avatarUrl)
            .select { Users.id eq userId }
            .singleOrNull()
            ?.get(Users.avatarUrl)

        if (avatarUrl != null) {
            val file = File("$uploadDir/$avatarUrl")
            if (file.exists()) file.delete()
        }

        Users.update({ Users.id eq userId }) {
            it[Users.avatarUrl] = null
        }
        true
    }
}