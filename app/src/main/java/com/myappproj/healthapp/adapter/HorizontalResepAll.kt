package com.myappproj.healthapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.MenuModel

class HorizontalResepAll(
    private val context: Context,
    private val navigateToDetail: ((MenuModel) -> Unit)? = null
) : RecyclerView.Adapter<HorizontalResepAll.ViewHolder>() {

    private var menuList: List<MenuModel> = listOf()

    fun setData(newMenuList: List<MenuModel>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }

    fun getData(): List<MenuModel> {
        return menuList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.resep_makanan_all, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]

        // Load image using Glide library
        Glide.with(context)
            .load(menu.imageURL)
            .placeholder(R.drawable.placeholder_img2)
            .centerCrop()
            .into(holder.menuImageView)

        holder.menuNameTextView.text = menu.menuName
        holder.calorieTextView.text = "${menu.calorieContent} kal"

        // Tambahkan listener klik
        holder.itemView.setOnClickListener {
            // Jika navigateToDetail disediakan, gunakan untuk navigasi
            navigateToDetail?.invoke(menu)
        }
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImageView: ImageView = itemView.findViewById(R.id.menu_img)
        val menuNameTextView: TextView = itemView.findViewById(R.id.nama_makanan)
        val calorieTextView: TextView = itemView.findViewById(R.id.jml_kalori)
    }
}