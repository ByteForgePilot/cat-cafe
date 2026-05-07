package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class LikeRepository {
    private val api = RetrofitClient.apiService

    suspend fun like(request: LikeCreateRequest): Result<LikeResponse> =
        apiCall { api.like(request) }

    suspend fun unlike(likeId: Int): Result<Unit> =
        apiCall { api.unlike(likeId) }

    suspend fun getLikes(
        likeType: Int? = null,
        skip: Int = 0,
        limit: Int = 20
    ): Result<PaginatedLikes> = apiCall { api.getLikes(likeType, skip, limit) }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception(response.errorBody()?.string() ?: "请求失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
