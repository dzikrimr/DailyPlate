package com.myappproj.healthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.ItemModel

class HorizontalRecyclerView(private var items: List<ItemModel>) : RecyclerView.Adapter<HorizontalRecyclerView.MyViewHolder>() {

    // Properti untuk menyimpan listener klik
    private var itemClickListener: ItemClickListener? = null

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val imageView: ImageView = itemView.findViewById(R.id.imgkeluhan)
        private val textView: TextView = itemView.findViewById(R.id.titleimg)

        init {
            // Menambahkan listener klik ke itemView
            itemView.setOnClickListener(this)
        }

        // Implementasi onClick untuk menangani klik pada item
        override fun onClick(v: View?) {
            itemClickListener?.onItemClick(items[adapterPosition])
        }

        fun bind(item: ItemModel) {
            // Load image using Glide library
            Glide.with(itemView)
                .load(item.imageURL)
                .placeholder(R.drawable.keluhan_imgholder) // Placeholder image while loading
                .centerCrop()
                .into(imageView)

            textView.text = item.diseases
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(newItems: List<ItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    // Metode untuk mengatur listener klik dari luar adapter
    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    // Interface untuk menangani klik pada item
    interface ItemClickListener {
        fun onItemClick(item: ItemModel)
    }
}


