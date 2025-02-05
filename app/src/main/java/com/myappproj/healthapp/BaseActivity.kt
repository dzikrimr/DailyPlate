package com.myappproj.healthapp

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        val sharedPref = newBase?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPref?.getString("app_language", "id") ?: "id"
        val locale = Locale(languageCode)

        val config = Configuration(newBase?.resources?.configuration)
        config.setLocale(locale)

        super.attachBaseContext(newBase?.createConfigurationContext(config))
    }
}