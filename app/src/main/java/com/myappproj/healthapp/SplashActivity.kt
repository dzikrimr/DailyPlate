package com.myappproj.healthapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen) // Pastikan Anda memiliki layout untuk splash

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Setel tema berdasarkan preferensi pengguna
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        auth = FirebaseAuth.getInstance()

        // Menunggu beberapa detik sebelum menavigasi
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigasi ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Tutup SplashActivity agar tidak bisa kembali
        }, 2000) // Tampilkan splash screen selama 2 detik
    }
}