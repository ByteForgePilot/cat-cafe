package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class CatRepository {
    private val api = RetrofitClient.apiService

    suspend fun getCats(skip: Int = 0, limit: Int = 20): Result<PaginatedCats> =
        apiCall { api.getCats(skip, limit) }

    suspend fun getCatDetail(catId: Int): Result<CatResponse> =
        apiCall { api.getCatDetail(catId) }

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
