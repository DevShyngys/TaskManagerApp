package com.example.tasko.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TipsApiClient {


    private const val BASE_URL =
        "https://raw.githubusercontent.com/DevShyngys/TaskManagerApp/master/"
    private val okHttp by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    val api: TipsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TipsApiService::class.java)
    }
}
