import config.AppConfig
import config.DataBase
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.routing.*
import java.io.File

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 1. Инициализация конфига (BASE_URL, UPLOAD_DIR)
    AppConfig.init(this)

    // 2. Сериализация JSON через Gson
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    // 3. Подключение к БД
    DataBase.init(this)

    // 4. Раздача статических файлов (фото, аватары, точки квестов)
    //    GET /uploads/photos/...  →  {uploadDir}/photos/...
    //    GET /uploads/avatars/... →  {uploadDir}/avatars/...
    //    GET /uploads/points/...  →  {uploadDir}/points/...
    routing {
        staticFiles("/uploads", File(AppConfig.uploadDir))
    }

    // 5. API-маршруты
    configureRouting()
}