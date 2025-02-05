package com.myappproj.healthapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.MenuModel

class VerticalResepAll(private val context: Context) :
    RecyclerView.Adapter<VerticalResepAll.ViewHolder>() {

    private var menuList: List<MenuModel> = listOf()
    private var onItemClickListener: ((String) -> Unit)? = null

    fun setData(newList: List<MenuModel>) {
        menuList = newList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.resep_makanan_all,
            parent,
            false
        )
        return ViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]
        holder.bind(menu)
    }

    override fun getItemCount(): Int = menuList.size

    inner class ViewHolder(
        itemView: View,
        private val onItemClickListener: ((String) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val namaMenu: TextView = itemView.findViewById(R.id.nama_makanan)
        private val gambarMenu: ImageView = itemView.findViewById(R.id.menu_img)
        private val kaloriMenu: TextView = itemView.findViewById(R.id.jml_kalori)

        fun bind(menu: MenuModel) {
            namaMenu.text = menu.menuName
            kaloriMenu.text = "${menu.calorieContent} kal"

            // Load gambar
            Glide.with(context)
                .load(menu.imageURL)
                .placeholder(R.drawable.placeholder_img2)
                .centerCrop()
                .into(gambarMenu)

            // Tambahkan listener klik pada seluruh item
            itemView.setOnClickListener {
                onItemClickListener?.invoke(menu.menuId)
            }
        }
    }
}