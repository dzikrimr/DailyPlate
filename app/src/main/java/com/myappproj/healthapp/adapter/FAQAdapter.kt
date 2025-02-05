package com.myappproj.healthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.FAQItem
import com.myappproj.healthapp.R

class FAQAdapter(private val faqList: List<FAQItem>) : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    private val expandedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faqItem = faqList[position]
        holder.bind(faqItem, expandedItems.contains(position))

        holder.itemView.setOnClickListener {
            if (expandedItems.contains(position)) {
                expandedItems.remove(position)
            } else {
                expandedItems.add(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.question_text)
        private val answerText: TextView = itemView.findViewById(R.id.answer_text)
        private val expandCollapseIcon: ImageView = itemView.findViewById(R.id.expand_collapse_icon)
        private val answerLayout: LinearLayout = itemView.findViewById(R.id.answer_layout)

        fun bind(faqItem: FAQItem, isExpanded: Boolean) {
            questionText.text = faqItem .question
            answerText.text = faqItem.answer
            answerLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            expandCollapseIcon.setImageResource(if (isExpanded) R.drawable.ic_collapse else R.drawable.ic_expand)
        }
    }
}