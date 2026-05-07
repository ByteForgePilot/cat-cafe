package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class AuthRepository {
    private val api = RetrofitClient.apiService

    suspend fun register(request: RegisterRequest): Result<LoginResponse> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "注册失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "登录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
