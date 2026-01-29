package config

import java.util.Base64
import kotlinx.serialization.json.*

object Security {

    fun generateToken(userId: String): String {
        val payload = buildJsonObject {
            put("user_id", userId)
            put("exp", System.currentTimeMillis() / 1000 + 60 * 60 * 24 * 30) // 30 дней
        }
        return Base64.getEncoder().encodeToString(payload.toString().toByteArray())
    }

    fun getUserId(token: String?): String? {
        if (token.isNullOrBlank()) return null

        return try {
            // ✅ Сначала проверяем старый формат "test-token-XXX"
            if (token.startsWith("test-token-")) {
                return token.removePrefix("test-token-")
            }

            // Новый формат: Base64-encoded JSON
            val json = String(Base64.getDecoder().decode(token))
            val obj = Json.parseToJsonElement(json).jsonObject
            val exp = obj["exp"]?.jsonPrimitive?.longOrNull ?: return null

            if (exp > System.currentTimeMillis() / 1000) {
                obj["user_id"]?.jsonPrimitive?.contentOrNull
            } else {
                null // Токен истёк
            }
        } catch (e: Exception) {
            // Если не удалось распарсить — возможно это старый формат без префикса
            println("Token parse error: ${e.message}")
            null
        }
    }
}
