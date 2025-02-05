package com.myappproj.healthapp

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*
import com.myappproj.healthapp.adapter.HorizontalResepAll
import com.myappproj.healthapp.model.MenuModel
import render.animations.Bounce
import render.animations.Render
import java.util.Locale

class TabResepFragment1 : Fragment() {

    private lateinit var recyclerViewMyMenu: RecyclerView
    private lateinit var recyclerViewMyMenu2: RecyclerView
    private lateinit var recyclerViewMyMenu3: RecyclerView
    private lateinit var menuAdapter1: HorizontalResepAll
    private lateinit var menuAdapter2: HorizontalResepAll
    private lateinit var menuAdapter3: HorizontalResepAll
    private lateinit var database: DatabaseReference
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var filterAiButton: ImageView
    private lateinit var bannerImg: ImageView
    private lateinit var cardView: CardView
    private lateinit var render: Render
    private lateinit var currentLanguage: String

    private var isDataLoaded = false
    private val dataLoadedList = mutableListOf<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment tab resep 1
        return inflater.inflate(R.layout.fragment_tab_resep1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi view
        shimmerFrameLayout = view.findViewById(R.id.shimmer_container)
        recyclerViewMyMenu = view.findViewById(R.id.recyclerview_mymenu)
        recyclerViewMyMenu2 = view.findViewById(R.id.recyclerview_mymenu2)
        recyclerViewMyMenu3 = view.findViewById(R.id.recyclerview_mymenu3)
        bannerImg = view.findViewById(R.id.banner_img)
        filterAiButton = view.findViewById(R.id.btn_filter)
        cardView = view.findViewById(R.id.card_banner)

        shimmerFrameLayout.startShimmer()

        // Mulai animasi banner
        startFloatingAnimation()

        cardView.alpha = 0f

        // Inisialisasi Render untuk animasi pada cardView
        render = Render(requireContext())

        // Set animasi pada cardView
        render.setAnimation((Bounce()).InUp(cardView))

        // Delay 1 detik sebelum animasi dimulai
        Handler(Looper.getMainLooper()).postDelayed({
            render.start() // Mulai animasi slide-in dan fade
        }, 300)

        bannerImg = view.findViewById(R.id.banner_img)

        // Mulai animasi
        startFloatingAnimation()

        // Set listener untuk tombol
        filterAiButton.setOnClickListener {
            findNavController().navigate(R.id.action_resepFragment_to_aiFilterFragment)
        }

        // Inisialisasi menuAdapters dengan listener navigasi
        menuAdapter1 = HorizontalResepAll(requireContext()) { menu ->
            navigateToMainResep(menu)
        }
        menuAdapter2 = HorizontalResepAll(requireContext()) { menu ->
            navigateToMainResep(menu)
        }
        menuAdapter3 = HorizontalResepAll(requireContext()) { menu ->
            navigateToMainResep(menu)
        }

        // Setup RecyclerViews
        setupRecyclerView(recyclerViewMyMenu, menuAdapter1)
        setupRecyclerView(recyclerViewMyMenu2, menuAdapter2)
        setupRecyclerView(recyclerViewMyMenu3, menuAdapter3)

        // Inisialisasi Database Reference
        database = FirebaseDatabase.getInstance().reference

        currentLanguage = Locale.getDefault().language

        // Muat data menu
        loadMenuData()
    }

    private fun loadMenuData() {
        isDataLoaded = false
        dataLoadedList.clear()
        dataLoadedList.add(false)
        dataLoadedList.add(false)
        dataLoadedList.add(false)

        val menuTypeList = getMenuTypeForLanguage(currentLanguage)

        val queries = listOf(
            // Makanan Rendah Kalori
            database.child("resep").orderByChild("menuType").equalTo(menuTypeList[0]),
            // Camilan Rendah Kalori
            database.child("resep").orderByChild("menuType").equalTo(menuTypeList[1]),
            // Minuman
            database.child("resep").orderByChild("menuType").equalTo(menuTypeList[2])
        )

        queries.forEachIndexed { index, query ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val menuList = mutableListOf<MenuModel>()
                    for (snapshot in dataSnapshot.children) {
                        val menu = snapshot.getValue(MenuModel::class.java)
                        when (index) {
                            0 -> { // Makanan Rendah Kalori
                                val calorieContent = menu?.calorieContent?.toDoubleOrNull() ?: 0.0
                                if (calorieContent <= 300) menu?.let { menuList.add(it) }
                            }
                            1 -> { // Camilan Rendah Kalori
                                val calorieContent = menu?.calorieContent?.toDoubleOrNull() ?: 0.0
                                if (calorieContent <= 200) menu?.let { menuList.add(it) }
                            }
                            2 -> { // Minuman
                                menu?.let { menuList.add(it) }
                            }
                        }
                    }

                    // Update data untuk masing-masing adapter
                    when (index) {
                        0 -> menuAdapter1.setData(menuList)  // Makanan Rendah Kalori
                        1 -> menuAdapter2.setData(menuList)  // Camilan Rendah Kalori
                        2 -> menuAdapter3.setData(menuList)  // Minuman
                    }

                    dataLoadedList[index] = true
                    checkAllDataLoaded()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("DataLoading", "Error loading data: ${databaseError.message}")
                    dataLoadedList[index] = true
                    showErrorDialog(databaseError.message)
                    checkAllDataLoaded()
                }
            })
        }

        // Timeout untuk memuat data
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isDataLoaded) {
                showErrorDialog("Timeout: Gagal memuat data")
                stopShimmerAndShowContent()
            }
        }, 10000) // 10 detik timeout
    }

    // Fungsi untuk menyesuaikan menuType berdasarkan bahasa yang aktif
    private fun getMenuTypeForLanguage(language: String): List<String> {
        return if (language == "id") {
            // Bahasa Indonesia
            listOf("Makanan", "Camilan", "Minuman")
        } else {
            // Bahasa Inggris
            listOf("Food", "Snack", "Drink")
        }
    }

    private fun checkAllDataLoaded() {
        if (dataLoadedList.all { it }) {
            stopShimmerAndShowContent()
        }
    }

    private fun startFloatingAnimation() {
        // Buat animasi naik dan turun
        val floatAnimator = ObjectAnimator.ofFloat(bannerImg, "translationY", -15f, 15f)
        floatAnimator.duration = 1500 // Durasi animasi (1.5 detik)
        floatAnimator.repeatCount = ObjectAnimator.INFINITE // Ulangi terus menerus
        floatAnimator.repeatMode = ObjectAnimator.REVERSE // Balik arah setelah selesai
        floatAnimator.interpolator = AccelerateDecelerateInterpolator() // Efek percepatan dan perlambatan

        // Mulai animasi
        floatAnimator.start()
    }


    private fun stopShimmerAndShowContent() {
        activity?.runOnUiThread {
            if (!isDataLoaded) {
                isDataLoaded = true
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                view?.findViewById<View>(R.id.actual_content)?.visibility = View.VISIBLE
            }
        }
    }

    private fun showErrorDialog(message: String) {
        activity?.runOnUiThread {
            AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Coba Lagi") { _, _ -> loadMenuData() }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: HorizontalResepAll) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    // Metode untuk navigasi ke halaman detail resep
    private fun navigateToMainResep(menu: MenuModel) {
        // Buat bundle untuk mengirim data menu
        val bundle = Bundle().apply {
            putString("menuId", menu.menuId)
            // Anda dapat menambahkan data lain yang diperlukan
        }

        // Navigasi ke fragment MainResepFragment
        findNavController().navigate(R.id.action_resepFragment_to_mainResepFragment, bundle)
    }
}