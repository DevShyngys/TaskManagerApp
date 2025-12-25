package com.example.tasko.data.network

class TipsRepository(
    private val api: TipsApiService = TipsApiClient.api
) {
    suspend fun loadTips(): List<TipDto> = api.getTips()
}
