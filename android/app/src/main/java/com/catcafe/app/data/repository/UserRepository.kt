package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class UserRepository {
    private val api = RetrofitClient.apiService

    suspend fun getMyProfile(): Result<UserDetailResponse> = apiCall { api.getMyProfile() }

    suspend fun updateProfile(request: UserUpdateRequest): Result<UserDetailResponse> =
        apiCall { api.updateProfile(request) }

    suspend fun changePassword(request: PasswordChangeRequest): Result<UserDetailResponse> =
        apiCall { api.changePassword(request) }

    suspend fun deleteAccount(): Result<Unit> = apiCall { api.deleteAccount() }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "请求失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
