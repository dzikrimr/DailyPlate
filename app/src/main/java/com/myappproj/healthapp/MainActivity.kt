package com.myappproj.healthapp

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private var backPressCount = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateConfiguration()

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Setel tema berdasarkan preferensi pengguna
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Mengatur warna status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.green)
        }

        if (!isInternetAvailable()) {
            showNoInternetDialog()
        }

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Setup Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)

        // Tambahkan listener untuk mengatur visibilitas BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.berandaFragment,
                R.id.searchFragment,
                R.id.resepFragment,
                R.id.profileFragment -> {
                    bottomNavigation.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigation.visibility = View.GONE
                }
            }
        }

        // Cek apakah pengguna sudah login
        if (auth.currentUser != null) {
            // Jika pengguna sudah login, tampilkan Bottom Navigation
            bottomNavigation.visibility = View.VISIBLE
            navController.navigate(R.id.berandaFragment)
        } else {
            // Jika pengguna belum login, sembunyikan Bottom Navigation dan arahkan ke OnBoardingFragment
            bottomNavigation.visibility = View.GONE
            navController.navigate(R.id.onBoardingFragment)
        }

        // Mengatur listener untuk bottom navigation view
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.berandaFragment)
                    true
                }
                R.id.search -> {
                    navController.navigate(R.id.searchFragment)
                    true
                }
                R.id.resep -> {
                    navController.navigate(R.id.resepFragment)
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

        // Mengatur back button agar keluar dari aplikasi dengan peringatan
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination = navController.currentDestination?.id
                if (currentDestination == R.id.onBoardingFragment) {
                    // Jika berada di OnBoardingFragment, keluar aplikasi
                    finish()
                } else if (currentDestination == R.id.berandaFragment ||
                    currentDestination == R.id.searchFragment ||
                    currentDestination == R.id.resepFragment ||
                    currentDestination == R.id.profileFragment
                ) {
                    // Jika berada di salah satu fragment utama, hitung back press
                    if (backPressCount == 0) {
                        backPressCount++
                        Toast.makeText(this@MainActivity, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    } else {
                        finish() // Keluar dari aplikasi
                    }
                } else {
                    // Jika tidak, lakukan navigasi kembali
                    navController.popBackStack()
                    backPressCount = 0 // Reset hitungan
                }
            }
        })
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)

        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        tvMessage.text = getString(R.string.no_internet)

        builder.setView(dialogView)
        val dialog = builder.create()

        val btnYes: Button = dialogView.findViewById(R.id.btn_yes)
        val btnNo = dialogView.findViewById<Button>(R.id.btn_no)

        btnNo.visibility = View.GONE
        btnYes.setOnClickListener {
            finish()
        }

        // Prevent closing the dialog by clicking outside
        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }


    private fun updateConfiguration() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPref.getString("app_language", "id") ?: "id"
        val locale = Locale(languageCode)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Metode untuk menampilkan Bottom Navigation
    fun showBottomNavigation() {
        bottomNavigation.visibility = View.VISIBLE
    }

    // Metode untuk menyembunyikan Bottom Navigation
    fun hideBottomNavigation() {
        bottomNavigation.visibility = View.INVISIBLE
    }

    fun logout() {
        // Logout dari Firebase
        auth.signOut()

        // Logout dari Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Pastikan Anda menggunakan activity context
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            // Sembunyikan Bottom Navigation saat logout
            bottomNavigation.visibility = View.GONE
            // Navigasi ke OnBoardingFragment
            navController.navigate(R.id.onBoardingFragment)

            // Hapus semua fragment dari back stack
            navController.popBackStack(R.id.onBoardingFragment, false)
        }
    }
}
