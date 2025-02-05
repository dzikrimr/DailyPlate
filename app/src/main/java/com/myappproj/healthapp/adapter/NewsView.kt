package com.myappproj.healthapp.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.Article

class NewsView(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
    private val tvImage = itemView.findViewById<ImageView>(R.id.tvImage)

    fun bind(article: Article) {
        // Set the title
        tvTitle.text = article.title

        Glide.with(itemView.context)
            .load(article.image_url)
            .into(tvImage)
    }
}
