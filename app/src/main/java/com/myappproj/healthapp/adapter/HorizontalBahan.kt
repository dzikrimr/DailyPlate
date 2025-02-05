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

class HorizontalBahan(private val context: Context) :
    RecyclerView.Adapter<HorizontalBahan.ViewHolder>() {

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

        // Load image using Glide library
        Glide.with(context)
            .load(bahan.imageURL)
            .placeholder(R.drawable.placeholder_img3)
            .centerCrop()
            .into(holder.bahanImageView)

        holder.bahanNameTextView.text = bahan.bahanName
        holder.descTextView.text = bahan.desc
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
