package com.example.mobilecomm

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(
    private val articles: MutableList<Post>
) : RecyclerView.Adapter<PostAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val title: TextView =
            view.findViewById(R.id.txtTitle)

        val source: TextView =
            view.findViewById(R.id.txtSource)

        val date: TextView =
            view.findViewById(R.id.txtDate)

        val description: TextView =
            view.findViewById(R.id.txtDescription)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)

        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NewsViewHolder,
        position: Int
    ) {
        val article = articles[position]

        holder.title.text =
            article.title ?: "Title unavailable"

        holder.source.text =
            "Source: ${article.source?.name ?: "Unknown"}"

        holder.date.text =
            "Published: ${article.publishedAt ?: "Unknown"}"

        holder.description.text =
            article.description ?: "No description available"

        holder.itemView.setOnClickListener {
            val url = article.url

            if (!url.isNullOrBlank()) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateNews(newArticles: List<Post>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }
}