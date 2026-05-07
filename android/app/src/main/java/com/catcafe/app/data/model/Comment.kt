package com.catcafe.app.data.model

import com.google.gson.annotations.SerializedName

data class CommentCreateRequest(
    @SerializedName("targetType") val targetType: Int,
    @SerializedName("targetId") val targetId: Int,
    @SerializedName("content") val content: String
)

data class CommentResponse(
    @SerializedName("commentId") val commentId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String?,
    @SerializedName("targetType") val targetType: Int,
    @SerializedName("targetId") val targetId: Int,
    @SerializedName("objectName") val objectName: String?,
    @SerializedName("content") val content: String,
    @SerializedName("auditStatus") val auditStatus: Int,
    @SerializedName("createTime") val createTime: String
)

data class PaginatedComments(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<CommentResponse>
)
