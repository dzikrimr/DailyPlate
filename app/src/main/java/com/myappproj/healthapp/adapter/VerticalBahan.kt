package com.myappproj.healthapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myappproj.healthapp.model.BahanModel
import com.myappproj.healthapp.R
import java.util.Locale

class VerticalBahan(private val context: Context) :
    RecyclerView.Adapter<VerticalBahan.ViewHolder>() {

    private var bahanList: List<BahanModel> = listOf()

    fun setData(newBahanList: List<BahanModel>) {
        bahanList = newBahanList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bahanrecycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bahan = bahanList[position]

        // Dapatkan bahasa yang aktif
        val currentLanguage = Locale.getDefault().language

        // Tentukan teks berdasarkan bahasa yang aktif
        val bahanName = if (currentLanguage == "en") bahan.bahanNameEn else bahan.bahanName
        val desc = if (currentLanguage == "en") bahan.descEn else bahan.desc

        // Load image using Glide library
        Glide.with(context)
            .load(bahan.imageURL)
            .placeholder(R.drawable.placeholder_img3)
            .centerCrop()
            .into(holder.bahanImageView)

        // Set teks nama bahan dan deskripsi sesuai bahasa
        holder.bahanNameTextView.text = bahanName
        holder.descTextView.text = desc
    }

    override fun getItemCount(): Int {
        return bahanList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bahanImageView: ImageView = itemView.findViewById(R.id.bahan_img)
        val bahanNameTextView: TextView = itemView.findViewById(R.id.bahan_name)
        val descTextView: TextView = itemView.findViewById(R.id.desc)
    }
}
