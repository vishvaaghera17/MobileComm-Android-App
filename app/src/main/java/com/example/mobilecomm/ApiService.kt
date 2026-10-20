package com.example.mobilecomm

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query

interface ApiService {

    @GET("everything")
    fun getLatestNews(
        @Query("q") query: String = "technology",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 30,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}