package dto




data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val userId: String? = null,
    val user: UserDto? = null,
    val message: String? = null
)




