package com.example.yunjing.data

import com.example.yunjing.network.AppServerConfig
import com.example.yunjing.ui.buyer.network.BuyerApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppServerConfig.backendBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AppServerConfig.backendBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val buyerApiService: BuyerApiService by lazy {
        retrofit.create(BuyerApiService::class.java)
    }
}
