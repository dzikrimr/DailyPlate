package com.myappproj.healthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.R

class BannerAdapter(private val bannerImages: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(bannerImages[position])
        // Terapkan animasi menggunakan library
    }

    override fun getItemCount(): Int = bannerImages.size

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bannerImage: ImageView = itemView.findViewById(R.id.banner_image)

        fun bind(imageResId: Int) {
            bannerImage.setImageResource(imageResId)
        }
    }
}