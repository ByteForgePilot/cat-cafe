package com.catcafe.app.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("userType") val userType: Int,
    @SerializedName("createTime") val createTime: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserResponse
)

data class UserDetailResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("gender") val gender: Int?,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("userType") val userType: Int,
    @SerializedName("createTime") val createTime: String
)

data class RegisterRequest(
    @SerializedName("userName") val userName: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String
)

data class LoginRequest(
    @SerializedName("userName") val userName: String,
    @SerializedName("password") val password: String
)

data class UserUpdateRequest(
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("gender") val gender: Int? = null,
    @SerializedName("birthday") val birthday: String? = null
)

data class PasswordChangeRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)
