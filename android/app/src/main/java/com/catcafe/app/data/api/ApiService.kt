package com.catcafe.app.data.api

import com.catcafe.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // User
    @GET("api/user/me")
    suspend fun getMyProfile(): Response<UserDetailResponse>

    @PUT("api/user/me")
    suspend fun updateProfile(@Body request: UserUpdateRequest): Response<UserDetailResponse>

    @PUT("api/user/me/password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): Response<UserDetailResponse>

    @DELETE("api/user/me")
    suspend fun deleteAccount(): Response<Unit>

    // Cats
    @GET("api/cats")
    suspend fun getCats(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedCats>

    @GET("api/cats/{catId}")
    suspend fun getCatDetail(@Path("catId") catId: Int): Response<CatResponse>

    // Products
    @GET("api/products")
    suspend fun getProducts(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null
    ): Response<PaginatedProducts>

    @GET("api/products/{productId}")
    suspend fun getProductDetail(@Path("productId") productId: Int): Response<ProductResponse>

    // Orders
    @POST("api/orders")
    suspend fun createOrder(@Body request: OrderCreateRequest): Response<BatchOrderResponse>

    @GET("api/orders")
    suspend fun getOrders(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("orderStatus") orderStatus: Int? = null
    ): Response<PaginatedOrders>

    @GET("api/orders/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: Int): Response<BatchOrderResponse>

    @PUT("api/orders/{orderId}")
    suspend fun updateOrder(
        @Path("orderId") orderId: Int,
        @Body request: OrderUpdateRequest
    ): Response<BatchOrderResponse>

    @DELETE("api/orders/{orderId}")
    suspend fun deleteOrder(@Path("orderId") orderId: Int): Response<Unit>

    // Comments
    @POST("api/comments")
    suspend fun createComment(@Body request: CommentCreateRequest): Response<CommentResponse>

    @GET("api/comments")
    suspend fun getComments(
        @Query("targetType") targetType: Int? = null,
        @Query("targetId") targetId: Int? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedComments>

    @DELETE("api/comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: Int): Response<Unit>

    // Likes
    @POST("api/likes")
    suspend fun like(@Body request: LikeCreateRequest): Response<LikeResponse>

    @DELETE("api/likes/{likeId}")
    suspend fun unlike(@Path("likeId") likeId: Int): Response<Unit>

    @GET("api/likes")
    suspend fun getLikes(
        @Query("likeType") likeType: Int? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedLikes>
}
