package com.catcafe.app.data.model

import com.google.gson.annotations.SerializedName

data class CatResponse(
    @SerializedName("catId") val catId: Int,
    @SerializedName("catName") val catName: String,
    @SerializedName("catBreed") val catBreed: String?,
    @SerializedName("catAge") val catAge: Int?,
    @SerializedName("catGender") val catGender: Int?,
    @SerializedName("catAvatar") val catAvatar: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: Int,
    @SerializedName("createTime") val createTime: String
)

data class PaginatedCats(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<CatResponse>
)
