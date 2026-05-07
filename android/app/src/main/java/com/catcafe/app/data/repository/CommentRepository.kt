package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class CommentRepository {
    private val api = RetrofitClient.apiService

    suspend fun createComment(request: CommentCreateRequest): Result<CommentResponse> =
        apiCall { api.createComment(request) }

    suspend fun getComments(
        targetType: Int? = null,
        targetId: Int? = null,
        skip: Int = 0,
        limit: Int = 20
    ): Result<PaginatedComments> = apiCall { api.getComments(targetType, targetId, skip, limit) }

    suspend fun deleteComment(commentId: Int): Result<Unit> =
        apiCall { api.deleteComment(commentId) }

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
