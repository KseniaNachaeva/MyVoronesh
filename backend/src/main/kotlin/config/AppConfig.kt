package config

import io.ktor.server.application.*

object AppConfig {
    // Базовый URL, по которому клиент обращается к серверу.
    // Приоритет: переменная окружения BASE_URL → значение из application.conf → fallback для эмулятора
    lateinit var baseUrl: String

    // Абсолютный путь к папке uploads на диске.
    // Приоритет: переменная окружения UPLOAD_DIR → папка uploads/ рядом с JAR
    lateinit var uploadDir: String

    fun init(application: Application) {
        baseUrl = System.getenv("BASE_URL")
            ?: application.environment.config
                .propertyOrNull("app.baseUrl")?.getString()
                    ?: "http://10.0.2.2:8080"

        uploadDir = System.getenv("UPLOAD_DIR")
            ?: System.getProperty("user.dir") + "/uploads"

        application.environment.log.info("AppConfig: baseUrl=$baseUrl")
        application.environment.log.info("AppConfig: uploadDir=$uploadDir")
    }
}