package com.example.yunjing.data

class AuthRepository {

    suspend fun login(username: String, password: String): AuthResponse {
        val response = RetrofitClient.api.login(
            LoginRequest(username = username, password = password)
        )
        return response.body() ?: AuthResponse(false, "服务器返回为空", null, null, null, null)
    }

    suspend fun register(username: String, password: String, role: UserRole): AuthResponse {
        val response = RetrofitClient.api.register(
            RegisterRequest(
                username = username,
                password = password,
                role = role.name
            )
        )
        return response.body() ?: AuthResponse(false, "服务器返回为空", null, null, null, null)
    }
}