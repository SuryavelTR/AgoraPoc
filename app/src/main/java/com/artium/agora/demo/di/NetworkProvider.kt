package com.artium.agora.demo.di

import com.artium.agora.demo.network.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkProvider {

    @Singleton
    @Provides
    fun provideRetrofitClient(): ApiClient = Retrofit.Builder()
        .baseUrl("https://62417a609b450ae27440813c.mockapi.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiClient::class.java)
}