package com.myappproj.healthapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myappproj.healthapp.adapter.FAQAdapter

class HelpCenterFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var faqAdapter: FAQAdapter
    private lateinit var backButton: ImageView
    private lateinit var emailButton: FrameLayout
    private lateinit var emailCard: CardView
    private lateinit var instagramButton: FrameLayout
    private lateinit var instagramCard: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        backButton = view.findViewById(R.id.back_arrow)
        emailButton = view.findViewById(R.id.email_button)
        emailCard = view.findViewById(R.id.email_card)
        instagramButton = view.findViewById(R.id.instagram_button)
        instagramCard = view.findViewById(R.id.instagram_card)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Inisialisasi adapter dengan data FAQ
        faqAdapter = FAQAdapter(getFAQList())
        recyclerView.adapter = faqAdapter

        // Set up email button click listener for animation
        emailButton.setOnClickListener {
            val isExpanding = emailCard.visibility != View.VISIBLE
            animateCardExpansion(emailCard, isExpanding)
        }

        // Set up email card click listener to send email
        emailCard.setOnClickListener {
            sendEmail()
        }

        // Set up Instagram button click listener for animation
        instagramButton.setOnClickListener {
            val isExpanding = instagramCard.visibility != View.VISIBLE
            animateCardExpansion(instagramCard, isExpanding)
        }

        // Set up Instagram card click listener to open Instagram
        instagramCard.setOnClickListener {
            openInstagram()
        }
    }

    private fun getFAQList(): List<FAQItem> {
        return listOf(
            FAQItem(getString(R.string.faq_question_1), getString(R.string.faq_answer_1)),
            FAQItem(getString(R.string.faq_question_2), getString(R.string.faq_answer_2)),
            FAQItem(getString(R.string.faq_question_3), getString(R.string.faq_answer_3)),
            FAQItem(getString(R.string.faq_question_4), getString(R.string.faq_answer_4)),
            FAQItem(getString(R.string.faq_question_5), getString(R.string.faq_answer_5)),
            FAQItem(getString(R.string.faq_question_6), getString(R.string.faq_answer_6)),
            FAQItem(getString(R.string.faq_question_7), getString(R.string.faq_answer_7)),
            FAQItem(getString(R.string.faq_question_8), getString(R.string.faq_answer_8))
        )
    }

    private val initialWidth = 0
    private val finalWidth = 300

    private fun animateCardExpansion(cardView: CardView, isExpanding: Boolean) {
        val startWidth = if (isExpanding) initialWidth else finalWidth
        val endWidth = if (isExpanding) finalWidth else initialWidth

        val valueAnimator = ValueAnimator.ofInt(startWidth, endWidth)
        valueAnimator.duration = 300 // Durasi animasi dalam milidetik
        valueAnimator.addUpdateListener { animation ->
            val width = animation.animatedValue as Int
            val layoutParams = cardView.layoutParams
            layoutParams.width = width
            cardView.layoutParams = layoutParams
        }

        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                if (isExpanding) {
                    cardView.visibility = View.VISIBLE
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isExpanding) {
                    cardView.visibility = View.INVISIBLE
                }
            }
        })

        valueAnimator.start()
    }

    private fun sendEmail() {
        val email = "menaraciptaid@gmail.com"
        val subject = ""
        val body = "Halo, Admin!"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // hanya aplikasi email yang bisa menangani ini
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak ada aplikasi email yang ditemukan", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun openInstagram() {
        val uri = Uri.parse("https://www.instagram.com/menaraapp")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak ada aplikasi Instagram yang ditemukan", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
data class FAQItem(val question: String, val answer: String)
