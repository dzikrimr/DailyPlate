package com.myappproj.healthapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.suke.widget.SwitchButton

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        backButton = view.findViewById(R.id.back_arrow)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        val darkModeSwitch = view.findViewById<SwitchButton>(R.id.dark_mode_switch)

        // Setel status switch berdasarkan preferensi yang disimpan
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Simpan preferensi pengguna
            sharedPreferences.edit().putBoolean("DarkMode", isChecked).apply()

            requireActivity().recreate()
        }

        return view
    }
}