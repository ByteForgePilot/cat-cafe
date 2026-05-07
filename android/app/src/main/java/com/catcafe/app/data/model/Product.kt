package com.catcafe.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("productId") val productId: Int,
    @SerializedName("productName") val productName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String?,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("status") val status: Int,
    @SerializedName("createTime") val createTime: String
)

data class PaginatedProducts(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<ProductResponse>
)
