package com.catcafe.app.data.repository

import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.data.model.*

class OrderRepository {
    private val api = RetrofitClient.apiService

    suspend fun createOrder(request: OrderCreateRequest): Result<BatchOrderResponse> =
        apiCall { api.createOrder(request) }

    suspend fun getOrders(
        skip: Int = 0,
        limit: Int = 20,
        orderStatus: Int? = null
    ): Result<PaginatedOrders> = apiCall { api.getOrders(skip, limit, orderStatus) }

    suspend fun getOrderDetail(orderId: Int): Result<BatchOrderResponse> =
        apiCall { api.getOrderDetail(orderId) }

    suspend fun updateOrder(orderId: Int, request: OrderUpdateRequest): Result<BatchOrderResponse> =
        apiCall { api.updateOrder(orderId, request) }

    suspend fun deleteOrder(orderId: Int): Result<Unit> =
        apiCall { api.deleteOrder(orderId) }

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
