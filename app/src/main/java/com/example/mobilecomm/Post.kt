package com.example.mobilecomm

data class NewsResponse(
    val status: String?,
    val totalResults: Int?,
    val articles: List<Post>?
)

data class Post(
    val source: NewsSource?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

data class NewsSource(
    val id: String?,
    val name: String?
)