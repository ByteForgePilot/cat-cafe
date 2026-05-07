package com.catcafe.app.data.model

import com.google.gson.annotations.SerializedName

data class LikeCreateRequest(
    @SerializedName("likeType") val likeType: Int,
    @SerializedName("objectId") val objectId: Int
)

data class LikeResponse(
    @SerializedName("likeId") val likeId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("likeType") val likeType: Int,
    @SerializedName("objectId") val objectId: Int,
    @SerializedName("objectName") val objectName: String?,
    @SerializedName("createTime") val createTime: String
)

data class PaginatedLikes(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<LikeResponse>
)
