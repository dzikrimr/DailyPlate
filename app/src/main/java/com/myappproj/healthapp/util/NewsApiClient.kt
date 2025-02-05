package com.myappproj.healthapp.util

import com.myappproj.healthapp.network.NewsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsApiClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsdata.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val newsApi: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }
}
