package config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

object DataBase {
    fun init(application: Application) {
        val cfg = application.environment.config

        val url = System.getenv("DB_URL")
            ?: cfg.propertyOrNull("database.url")?.getString()
            ?: "jdbc:mysql://localhost:3306/MyVoronesh?useSSL=false&serverTimezone=UTC"

        val user = System.getenv("DB_USER")
            ?: cfg.propertyOrNull("database.user")?.getString()
            ?: "root"

        val password = System.getenv("DB_PASSWORD")
            ?: cfg.propertyOrNull("database.password")?.getString()
            ?: "mysql"

        Database.connect(
            url = url,
            driver = "com.mysql.cj.jdbc.Driver",
            user = user,
            password = password
        )

        application.environment.log.info("Database connected: $url")
    }
}