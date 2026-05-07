package com.catcafe.app.data.model

import com.google.gson.annotations.SerializedName

data class OrderItem(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class OrderCreateRequest(
    @SerializedName("items") val items: List<OrderItem>,
    @SerializedName("userPhone") val userPhone: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("orderNote") val orderNote: String? = null
)

data class OrderDetailItem(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("batchNo") val batchNo: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("productName") val productName: String,
    @SerializedName("price") val price: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("orderStatus") val orderStatus: Int,
    @SerializedName("userPhone") val userPhone: String?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("orderNote") val orderNote: String?,
    @SerializedName("createTime") val createTime: String,
    @SerializedName("paymentTime") val paymentTime: String?,
    @SerializedName("completionTime") val completionTime: String?
)

data class BatchOrderResponse(
    @SerializedName("batchNo") val batchNo: String,
    @SerializedName("items") val items: List<OrderDetailItem>,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("orderStatus") val orderStatus: Int,
    @SerializedName("createTime") val createTime: String
)

data class PaginatedOrders(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<BatchOrderResponse>
)

data class OrderUpdateRequest(
    @SerializedName("userPhone") val userPhone: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("orderNote") val orderNote: String? = null
)
