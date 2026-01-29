package repository

import dto.UserDto
import models.UserAuth
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDate
import java.util.*

object Users : Table("users") {
    val id = varchar("id", 36)
    val login = varchar("login", 50)
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)
    val email = varchar("email", 100).nullable()
    val birthDate = date("birth_date").nullable()
    val avatarUrl = varchar("avatar_url", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

object UserRepository {

    // ✅ Добавляем BASE_URL
    private const val BASE_URL = "http://10.0.2.2/myvoronesh_api"

    fun findAuthByLogin(loginValue: String): UserAuth? = transaction {
        Users
            .select { Users.login eq loginValue }
            .map {
                UserAuth(
                    id = it[Users.id],
                    login = it[Users.login],
                    passwordHash = it[Users.passwordHash],
                    name = it[Users.name]
                )
            }
            .singleOrNull()
    }

    fun findById(userId: String): UserDto? = transaction {
        Users
            .select { Users.id eq userId }
            .map {
                val avatarUrl = it[Users.avatarUrl]

                // ✅ Формируем полный URL для аватарки
                val avatarFullUrl = if (avatarUrl != null) {
                    "$BASE_URL/$avatarUrl?t=${System.currentTimeMillis()}"
                } else {
                    null
                }

                UserDto(
                    id = it[Users.id],
                    login = it[Users.login],
                    name = it[Users.name],
                    email = it[Users.email],
                    birthDate = it[Users.birthDate]?.toString(),
                    avatarUrl = avatarUrl,
                    avatarFullUrl = avatarFullUrl  // ✅ Добавляем!
                )
            }
            .singleOrNull()
    }

    fun createUser(login: String, password: String): UserDto? = transaction {
        val id = UUID.randomUUID().toString()
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())

        Users.insert {
            it[Users.id] = id
            it[Users.login] = login
            it[Users.passwordHash] = hash
            it[Users.name] = login
        }

        UserDto(
            id = id,
            login = login,
            name = login,
            email = null,
            birthDate = null,
            avatarUrl = null,
            avatarFullUrl = null
        )
    }

    fun updateProfile(
        userId: String,
        name: String,
        email: String?,
        birthDate: String?
    ): UserDto? = transaction {

        val parsedDate = birthDate?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        Users.update({ Users.id eq userId }) {
            it[Users.name] = name
            it[Users.email] = email
            it[Users.birthDate] = parsedDate
        }

        // ✅ Возвращаем с avatarFullUrl
        Users
            .select { Users.id eq userId }
            .map {
                val avatarUrl = it[Users.avatarUrl]
                val avatarFullUrl = if (avatarUrl != null) {
                    "$BASE_URL/$avatarUrl?t=${System.currentTimeMillis()}"
                } else {
                    null
                }

                UserDto(
                    id = it[Users.id],
                    login = it[Users.login],
                    name = it[Users.name],
                    email = it[Users.email],
                    birthDate = it[Users.birthDate]?.toString(),
                    avatarUrl = avatarUrl,
                    avatarFullUrl = avatarFullUrl
                )
            }
            .singleOrNull()
    }
}

