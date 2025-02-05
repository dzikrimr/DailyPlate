package com.myappproj.healthapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.R

class HistoryAdapter(
    private val context: Context,
    private val historyList: List<String>,
    private val listener: OnItemClickListener // Tambahkan parameter untuk listener
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    // Interface untuk mendengarkan klik pada item histori
    interface OnItemClickListener {
        fun onItemClick(item: String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val textViewHistoryItem: TextView = itemView.findViewById(R.id.textViewHistoryItem)

        init {
            // Atur pendengar klik untuk itemView
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = historyList[position]
                    listener.onItemClick(item) // Panggil fungsi onItemClick di listener
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.textViewHistoryItem.text = historyItem
        // Set gambar ikon
        holder.imageViewIcon.setImageResource(R.drawable.symbols_history)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

}


