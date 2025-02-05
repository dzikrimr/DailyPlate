package com.myappproj.healthapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class BahasaFragment : Fragment() {

    private lateinit var checkmarkHeader: ImageView
    private var selectedLanguage: String = "id" // Variabel untuk menyimpan bahasa yang dipilih

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bahasa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi view
        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)
        val titleTextView = view.findViewById<TextView>(R.id.title)
        val languageIndonesia = view.findViewById<LinearLayout>(R.id.language_indonesia)
        val languageEnglish = view.findViewById<LinearLayout>(R.id.language_english)
        val checkmarkIndonesia = view.findViewById<ImageView>(R.id.checkmark_indonesia)
        val checkmarkEnglish = view.findViewById<ImageView>(R.id.checkmark_english)
        checkmarkHeader = view.findViewById(R.id.checkmark_header)

        // Update UI awal
        updateCheckmarks(checkmarkIndonesia, checkmarkEnglish)

        // Handle tombol kembali
        backArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Handle pemilihan bahasa
        languageIndonesia.setOnClickListener {
            // Tampilkan checkmark untuk bahasa Indonesia
            checkmarkIndonesia.visibility = View.VISIBLE
            checkmarkEnglish.visibility = View.GONE
            selectedLanguage = "id" // Simpan pilihan bahasa
        }

        languageEnglish.setOnClickListener {
            // Tampilkan checkmark untuk bahasa Inggris
            checkmarkIndonesia.visibility = View.GONE
            checkmarkEnglish.visibility = View.VISIBLE
            selectedLanguage = "en" // Simpan pilihan bahasa
        }

        // Handle checkmark di header untuk menerapkan bahasa
        checkmarkHeader.setOnClickListener {
            setAppLanguage(selectedLanguage) // Terapkan bahasa yang dipilih
            requireActivity().recreate() // Restart activity untuk menerapkan bahasa
        }
    }

    private fun updateCheckmarks(indonesiaCheck: ImageView, englishCheck: ImageView) {
        when (getCurrentLanguage()) {
            "id" -> {
                indonesiaCheck.visibility = View.VISIBLE
                englishCheck.visibility = View.GONE
            }
            "en" -> {
                indonesiaCheck.visibility = View.GONE
                englishCheck.visibility = View.VISIBLE
            }
        }
    }

    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = requireContext().resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        requireContext().createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        saveLanguagePreference(languageCode)
    }

    private fun getCurrentLanguage(): String {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("app_language", "id") ?: "id"
    }

    private fun saveLanguagePreference(languageCode: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("app_language", languageCode)
            apply()
        }
    }
}