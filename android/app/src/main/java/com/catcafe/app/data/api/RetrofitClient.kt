package com.catcafe.app.data.api

import com.catcafe.app.util.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var tokenManager: TokenManager? = null
    private var retrofit: Retrofit? = null

    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
            tokenManager.getTokenBlocking()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(com.catcafe.app.util.Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService
        get() = retrofit!!.create(ApiService::class.java)
}
