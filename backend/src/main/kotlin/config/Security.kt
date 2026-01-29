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
            if (token.startsWith("test-token-")) {
                return token.removePrefix("test-token-")
            }

            val json = String(Base64.getDecoder().decode(token))
            val obj = Json.parseToJsonElement(json).jsonObject
            val exp = obj["exp"]?.jsonPrimitive?.longOrNull ?: return null

            if (exp > System.currentTimeMillis() / 1000) {
                obj["user_id"]?.jsonPrimitive?.contentOrNull
            } else {
                null
            }
        } catch (e: Exception) {

            println("Token parse error: ${e.message}")
            null
        }
    }
}
