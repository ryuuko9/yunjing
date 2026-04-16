package com.example.yunjing.data

data class LoginRequest(
    val username: String,
    val password: String,
    val role: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: Long?,
    val username: String?,
    val role: String?,
    val nickname: String?
)
