package com.myappproj.healthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.R
import com.myappproj.healthapp.model.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> UserMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user_message, parent, false)
            )
            else -> AIMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ai_message, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    // ViewHolder untuk pesan pengguna
    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_user_message)

        fun bind(message: ChatMessage) {
            messageText.text = message.text
        }
    }

    // ViewHolder untuk pesan AI
    class AIMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_ai_message)

        fun bind(message: ChatMessage) {
            messageText.text = message.text
        }
    }
}