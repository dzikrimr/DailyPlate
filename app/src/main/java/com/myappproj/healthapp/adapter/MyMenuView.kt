package com.myappproj.healthapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.MenuModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyMenuView(private val listener: MenuClickListener? = null) : RecyclerView.Adapter<MyMenuView.ViewHolder>() {
    private var menuList = emptyList<MenuModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_resep_recyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMenu = menuList[position]
        // Set data ke tampilan di dalam ViewHolder
        Glide.with(holder.itemView.context)
            .load(currentMenu.imageURL)
            .placeholder(R.drawable.placeholder_img)
            .centerCrop()
            .into(holder.imageView)
        holder.namaMakananTextView.text = currentMenu.menuName
        holder.jmlKaloriTextView.text = currentMenu.calorieContent + " cal"
        holder.tagMenuTextView.text = currentMenu.menuType

        // Set onClickListener di dalam onBindViewHolder
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val menuId = menuList[position].menuId
                listener?.onMenuClicked(menuId)
            }
        }
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    fun setData(newMenuList: List<MenuModel>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }

    // Metode untuk menghapus item
    fun deleteItem(position: Int) {
        if (position < 0 || position >= menuList.size) return

        val menuToDelete = menuList[position]
        val ref = FirebaseDatabase.getInstance().getReference("resep")

        // Menggunakan menuId langsung sebagai path untuk menghapus
        // karena menuId sama dengan key di Firebase
        ref.child(menuToDelete.menuId).removeValue()
            .addOnSuccessListener {
                Log.d("DeleteSuccess", "Menu deleted: ${menuToDelete.menuId}")
                // Biarkan ValueEventListener di Fragment yang handle update UI
            }
            .addOnFailureListener { e ->
                Log.e("DeleteError", "Failed to delete menu", e)
                notifyItemChanged(position)
            }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.menu_img)
        val namaMakananTextView: TextView = itemView.findViewById(R.id.nama_makanan)
        val jmlKaloriTextView: TextView = itemView.findViewById(R.id.jml_kalori)
        val tagMenuTextView: TextView = itemView.findViewById(R.id.tagmenu)
    }

    interface MenuClickListener {
        fun onMenuClicked(menuId: String)
    }
}