package com.myappproj.healthapp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.HealthNewsFragment
import com.myappproj.healthapp.NewsItemClickListener
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.Article

class NewsAdapter(private val context: Context, private val newsList: List<Article>) : RecyclerView.Adapter<NewsView>(),
    NewsItemClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsView{
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.newsrecycler, parent, false)
        return NewsView(view)
    }

    override fun onBindViewHolder(holder: NewsView, position: Int) {
        val article = newsList[position]
        holder.bind(article)

        holder.tvTitle.setOnClickListener {
            val url = article.link
            if (url != null) {
                // Open the URL in a web browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        }
    }

    override fun onNewsItemClicked(url: String?) {
        if (url != null) {
            val intent = Intent(context, HealthNewsFragment::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = newsList.size
}

