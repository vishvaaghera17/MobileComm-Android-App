package com.example.mobilecomm

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class RestApiActivity : AppCompatActivity() {
    private lateinit var adapter: PostAdapter
    private lateinit var status: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rest_api)

        status = findViewById(R.id.txtStatus)
        refreshButton = findViewById(R.id.btnRefresh)

        val recycler =
            findViewById<RecyclerView>(R.id.recyclerNews)

        adapter = PostAdapter(mutableListOf())

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        refreshButton.setOnClickListener {
            loadLatestNews()
        }

        loadLatestNews()
    }

    private fun loadLatestNews() {

        if (RetrofitClient.API_KEY == "YOUR_API_KEY") {
            status.text = "Please add your NewsAPI key."
            return
        }

        refreshButton.isEnabled = false
        status.text = "Loading latest news..."

        RetrofitClient.apiService.getLatestNews(
            query = "technology",
            language = "en",
            sortBy = "publishedAt",
            pageSize = 30,
            apiKey = RetrofitClient.API_KEY
        ).enqueue(object : Callback<NewsResponse> {

            override fun onResponse(
                call: Call<NewsResponse>,
                response: Response<NewsResponse>
            ) {
                refreshButton.isEnabled = true

                if (response.isSuccessful) {

                    val news = response.body()?.articles
                        ?.filter {
                            !it.title.isNullOrBlank() &&
                                    it.title != "[Removed]"
                        }
                        .orEmpty()

                    if (news.isEmpty()) {
                        status.text = "No news articles found."
                    } else {
                        adapter.updateNews(news)
                        status.text =
                            "Showing ${news.size} news articles"
                    }

                } else {
                    status.text =
                        "API error ${response.code()}: " +
                                "Check your API key, plan and request."
                }
            }

            override fun onFailure(
                call: Call<NewsResponse>,
                t: Throwable
            ) {
                refreshButton.isEnabled = true
                status.text =
                    "Network error: ${t.localizedMessage ?: "Please try again"}"
            }
        })
    }
}