package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class ProductRepository {
    private val api = RetrofitClient.apiService

    suspend fun getProducts(
        skip: Int = 0,
        limit: Int = 20,
        category: String? = null
    ): Result<PaginatedProducts> = apiCall { api.getProducts(skip, limit, category) }

    suspend fun getProductDetail(productId: Int): Result<ProductResponse> =
        apiCall { api.getProductDetail(productId) }

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
