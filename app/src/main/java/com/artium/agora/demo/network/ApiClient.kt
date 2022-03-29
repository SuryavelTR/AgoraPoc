package com.artium.agora.demo.network

import retrofit2.http.GET

interface ApiClient {

    @GET("api/v1/agora-poc/call-info")
    suspend fun fetchCallInfo(): ChannelInfo
}

data class ChannelInfo(val appId: String, val channelName: String, val token: String)
