package com.example.tasko.data.network

import retrofit2.http.GET

interface TipsApiService {
    @GET("tips.json")
    suspend fun getTips(): List<TipDto>
}
