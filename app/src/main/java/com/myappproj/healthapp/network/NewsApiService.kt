package com.myappproj.healthapp.network

import com.myappproj.healthapp.model.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("api/1/news")
    fun getTopHeadlines(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("apikey") apikey: String
    ): Call<News>
}