package com.myappproj.healthapp

import KadarFilterBottomSheet
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.database.*
import com.myappproj.healthapp.adapter.VerticalResepAll
import com.myappproj.healthapp.model.MenuModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AiFilterFragment : Fragment() {
    private lateinit var recyclerViewMenu: RecyclerView
    private lateinit var menuAdapter: VerticalResepAll
    private lateinit var database: DatabaseReference
    private lateinit var generativeModel: GenerativeModel
    private var displayedRecipes = mutableListOf<MenuModel>()

    // Filter TextView
    private lateinit var filterKadar: TextView
    private val filterItems = mutableListOf<TextView>()
    private lateinit var nutrisiList: List<String>

    // Penyimpan filter yang dipilih
    private val selectedFilters = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Gemini Model
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.apiKey
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nutrisiList = listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_carbohydrates),
            getString(R.string.filter_protein),
            getString(R.string.filter_fat),
            getString(R.string.filter_fiber),
            getString(R.string.filter_water),
            getString(R.string.filter_vitamin),
            getString(R.string.filter_mineral),
            getString(R.string.filter_calcium)
        )

        return inflater.inflate(R.layout.fragment_ai_filter, container, false)
    }

    private fun setupRecyclerView() {
        view?.let { currentView ->
            recyclerViewMenu = currentView.findViewById(R.id.recyclerview_mymenu2)
            menuAdapter = VerticalResepAll(requireContext())
            recyclerViewMenu.layoutManager = GridLayoutManager(
                requireContext(),
                2, // 2 kolom
                GridLayoutManager.VERTICAL, // Orientasi vertikal
                false
            )
            recyclerViewMenu.adapter = menuAdapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)

        backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_aiFilterFragment_to_resepFragment )
        }

        setupRecyclerView()

        menuAdapter.setOnItemClickListener { menuId ->
            navigateToMainResepFragment(menuId)
        }

        // Inisialisasi database
        database = FirebaseDatabase.getInstance().reference

        // Inisialisasi filter TextView
        filterKadar = view.findViewById(R.id.filter_kadar)

        // Daftar filter item
        val filterItemIds = listOf(
            R.id.filter_item_1, R.id.filter_item_2, R.id.filter_item_3,
            R.id.filter_item_4, R.id.filter_item_5, R.id.filter_item_6,
            R.id.filter_item_7, R.id.filter_item_8, R.id.filter_item_9
        )

        // Clear filterItems sebelum menambahkan
        filterItems.clear()

        filterItemIds.forEachIndexed { index, id ->
            val filterItem = view.findViewById<TextView>(id)
            filterItem.text = if (index < nutrisiList.size) nutrisiList[index] else getString(R.string.no_results)
            filterItems.add(filterItem)

            // Set background default
            filterItem.setBackgroundResource(R.drawable.bg_stroke_rounded20)

            // Atur listener untuk setiap filter nutrisi
            filterItem.setOnClickListener {
                toggleNutrisiFilter(filterItem, nutrisiList[index])
            }
        }

        // Cek apakah filterItems tidak kosong sebelum mengakses
        if (filterItems.isNotEmpty()) {
            // Set default filter "Semua"
            val semuaFilter = filterItems.first()
            semuaFilter.setBackgroundResource(R.drawable.bg_primary_rounded_20)
            selectedFilters[getString(R.string.filter_all)] = "Aktif"
        }

        // Atur listener untuk filter kadar
        filterKadar.setOnClickListener {
            showKadarFilterDialog()
        }

        // Muat data awal
        loadMenuData()
    }

    private fun navigateToMainResepFragment(menuId: String) {
        val bundle = Bundle()
        bundle.putString("menuId", menuId)

        findNavController().navigate(R.id.action_aiFilterFragment_to_mainResepFragment, bundle)
    }

    private fun toggleNutrisiFilter(filterView: TextView, nutrisi: String) {
        // Jika "Semua" dipilih, tampilkan semua resep
        if (nutrisi == getString(R.string.filter_all)) {
            loadMenuData() // Ini akan memuat semua resep
            filterItems.forEach { item ->
                item.setBackgroundResource(R.drawable.bg_stroke_rounded20)
                item.text = item.text.toString().replace(" (Tinggi)", "").replace(" (Rendah)", "")
            }
            filterView.setBackgroundResource(R.drawable.bg_primary_rounded_20)
            selectedFilters.clear() // Hapus semua filter
            selectedFilters[getString(R.string.filter_all)] = "Aktif"
            updateNutrisiText() // Update teks nutrisi
            return
        }

        // Cek apakah kadar sudah dipilih
        val kadarFilter = selectedFilters["Kadar"]

        // Toggle filter untuk nutrisi yang dipilih
        if (selectedFilters.containsKey(nutrisi)) {
            // Jika sudah dipilih, hapus filter
            selectedFilters.remove(nutrisi)
            filterView.setBackgroundResource(R.drawable.bg_stroke_rounded20)
            filterView.text = nutrisi
        } else {
            // Jika belum dipilih, tambahkan filter
            if (kadarFilter != null) {
                // Jika kadar sudah dipilih, tambahkan dengan kadar
                selectedFilters[nutrisi] = kadarFilter
                filterView.text = "$nutrisi ($kadarFilter)"
            } else {
                // Jika kadar belum dipilih, tambahkan tanpa kadar
                selectedFilters[nutrisi] = "Aktif"
            }
            filterView.setBackgroundResource(R.drawable.bg_primary_rounded_20)
        }

        // Hapus filter "Semua" jika ada nutrisi lain yang dipilih
        selectedFilters.remove(getString(R.string.filter_all))

        // Update tampilan filter yang dipilih
        updateFilterView()
        updateNutrisiText() // Update teks nutrisi

        // Cek apakah tidak ada nutrisi yang terselect
        val nutrisiFilters = selectedFilters.filter {
            it.key != getString(R.string.filter_all) &&
                    it.key != "Kadar" &&
                    nutrisiList.contains(it.key)
        }

        if (nutrisiFilters.isEmpty()) {
            // Tidak ada nutrisi yang terselect, aktifkan tablet 'Semua'
            val semuaFilter = filterItems.first()
            semuaFilter.setBackgroundResource(R.drawable.bg_primary_rounded_20)
            selectedFilters.clear()
            selectedFilters[getString(R.string.filter_all)] = "Aktif"

            // Tampilkan semua resep
            loadMenuData()
        } else {
            // Terapkan filter
            applyFilters()
        }
    }

    private fun updateNutrisiText() {
        val nutrisiFilter = selectedFilters.keys.find { it != "Kadar" && it != getString(R.string.filter_all) }
        val kadarFilter = selectedFilters["Kadar"]

        val nutrisiText = when {
            nutrisiFilter != null && kadarFilter != null -> "$kadarFilter $nutrisiFilter"
            nutrisiFilter != null -> nutrisiFilter
            kadarFilter != null -> kadarFilter
            else -> getString(R.string.filter_all)
        }

        val tvNutrisi: TextView = view?.findViewById(R.id.tv_nutrisi) ?: return
        tvNutrisi.text = nutrisiText
    }

    private fun updateFilterView() {
        // Reset semua filter
        filterItems.forEach { item ->
            item.setBackgroundResource(R.drawable.bg_stroke_rounded20)
            item.text = item.text.toString().replace(" (Tinggi)", "").replace(" (Rendah)", "")
        }

        // Ambil filter nutrisi yang aktif
        val nutrisiFilters = selectedFilters.filter {
            it.key != getString(R.string.filter_all) &&
                    it.key != "Kadar" &&
                    nutrisiList.contains(it.key)
        }

        // Dapatkan kadar filter jika ada
        val kadarFilter = selectedFilters["Kadar"]

        // Jika tidak ada filter nutrisi spesifik, kembalikan filter "Semua"
        if (nutrisiFilters.isEmpty()) {
            val semuaFilter = filterItems.first()

            // Tetap hijau untuk tablet "Semua"
            semuaFilter.text = getString(R.string.filter_all)
            semuaFilter.setBackgroundResource(R.drawable.bg_primary_rounded_20)
        } else {
            // Update tampilan filter yang dipilih
            nutrisiFilters.forEach { (nutrisi, filterValue) ->
                val filterView = filterItems.find { it.text.toString().startsWith(nutrisi) }
                filterView?.let {
                    it.setBackgroundResource(R.drawable.bg_primary_rounded_20)

                    // Tambahkan kadar jika ada
                    if (kadarFilter != null) {
                        it.text = "$nutrisi ($kadarFilter)"
                    } else {
                        it.text = nutrisi
                    }
                }
            }
        }

        // Update tablet kadar
        if (kadarFilter != null) {
            filterKadar.setBackgroundResource(R.drawable.bg_primary_rounded_20)
            filterKadar.text = "${getString(R.string.kadar_filter)} ($kadarFilter)"
        } else {
            filterKadar.setBackgroundResource(R.drawable.bg_stroke_rounded20)
            filterKadar.text = getString(R.string.kadar_filter)
        }
    }

    private fun showKadarFilterDialog() {
        val bottomSheet = KadarFilterBottomSheet()
        bottomSheet.setOnFilterAppliedListener { selectedKadar ->
            // Sembunyikan pesan "Hasil filter tidak ada yang relevan" jika ada
            val noResultsTextView = view?.findViewById<TextView>(R.id.tv_no_results)
            noResultsTextView?.visibility = View.GONE
            recyclerViewMenu.visibility = View.VISIBLE

            if (selectedKadar != null) {
                // Update UI filter kadar
                filterKadar.text = "${getString(R.string.kadar_filter)} ($selectedKadar)"
                filterKadar.setBackgroundResource(R.drawable.bg_primary_rounded_20)

                // Simpan filter kadar
                selectedFilters["Kadar"] = selectedKadar
            } else {
                // Jika tidak ada kadar yang dipilih (reset)
                filterKadar.text = getString(R.string.kadar_filter)
                filterKadar.setBackgroundResource(R.drawable.bg_stroke_rounded)

                // Hapus filter kadar
                selectedFilters.remove("Kadar")

                // Reset semua filter nutrisi
                resetAllFilters()
            }

            // Update semua tablet nutrisi yang sudah dipilih dengan kadar
            updateFilterView()
            updateNutrisiText() // Update teks nutrisi

            // Terapkan filter atau muat semua resep
            if (selectedFilters.isEmpty()) {
                loadMenuData() // Tampilkan semua resep
            } else {
                applyFilters()
            }
        }

        // Tampilkan bottom sheet
        bottomSheet.show(childFragmentManager, KadarFilterBottomSheet.TAG)
    }

    private fun resetAllFilters() {
        // Reset semua filter nutrisi
        filterItems.forEach { item ->
            item.setBackgroundResource(R.drawable.bg_stroke_rounded20)

            // Reset teks ke nama asli
            val originalText = when (item.id) {
                R.id.filter_item_1 -> getString(R.string.filter_all)
                R.id.filter_item_2 -> getString(R.string.filter_carbohydrates)
                R.id.filter_item_3 -> getString(R.string.filter_protein)
                R.id.filter_item_4 -> getString(R.string.filter_fat)
                R.id.filter_item_5 -> getString(R.string.filter_fiber)
                R.id.filter_item_6 -> getString(R.string.filter_water)
                R.id.filter_item_7 -> getString(R.string.filter_vitamin)
                R.id.filter_item_8 -> getString(R.string.filter_mineral)
                R.id.filter_item_9 -> getString(R.string.filter_calcium)
                else -> getString(R.string.no_results)
            }
            item.text = originalText
        }

        // Set filter "Semua" sebagai aktif
        val semuaFilter = filterItems.first()
        semuaFilter.setBackgroundResource(R.drawable.bg_primary_rounded_20)

        // Bersihkan semua filter
        selectedFilters.clear()
        selectedFilters[getString(R.string.filter_all)] = "Aktif"
    }

    private fun buildFilterPrompt(
        nutrisiFilters: Map<String, String>,
        kadarFilter: String?
    ): String {
        // Ambil nama-nama resep yang sedang ditampilkan
        val recipeNames = displayedRecipes.map { it.menuName }.joinToString(", ")

        // Bangun deskripsi filter
        val filterDetails = buildList {
            // Tambahkan filter nutrisi jika ada
            if (nutrisiFilters.isNotEmpty()) {
                add("Nutrisi: ${nutrisiFilters.keys.joinToString(", ")}")
            }

            // Tambahkan filter kadar jika ada
            if (kadarFilter != null) {
                add("Kadar: $kadarFilter")
            }
        }.joinToString(", ")

        return """
    Saya memiliki daftar resep berikut: $recipeNames
    
    Kriteria filter:
    $filterDetails
    
    Tolong bantu saya memilih resep yang sesuai dengan kriteria di atas:
    
    Petunjuk filtering:
    - Jika hanya ada filter nutrisi: 
      * Tampilkan semua resep yang mengandung nutrisi tersebut (baik tinggi maupun rendah)
    - Jika hanya ada filter kadar: 
      * Tampilkan semua resep dengan kadar nutrisi sesuai (dari semua jenis nutrisi)
    - Jika ada filter nutrisi dan kadar:
      * Tampilkan resep yang sesuai dengan kedua kriteria
    
    Berikan jawaban dalam format JSON:
    {
        "resep": ["Nama Resep 1", "Nama Resep 2"]
    }
    
    Pertimbangkan konteks dan keseluruhan kriteria.
    """.trimIndent()
    }

    private fun parseAIResponse(responseText: String): List<MenuModel> {
        return try {
            // Log respons asli untuk debugging
            Log.d("AIResponse", "Raw Response: $responseText")

            // Cek apakah respons menunjukkan tidak ada resep yang sesuai
            val noRelevantRecipesPatterns = listOf(
                "tidak ada resep yang dapat difilter",
                "tidak ada resep yang sesuai",
                "tidak ada informasi yang cukup",
                "semua resep tidak memenuhi kriteria"
            )

            // Jika respons mengandung salah satu pola di atas, kembalikan list kosong
            if (noRelevantRecipesPatterns.any { responseText.contains(it, ignoreCase = true) }) {
                Log.d("AIResponse", "No relevant recipes found")
                return emptyList()
            }

            // Coba ekstrak nama resep dari berbagai format
            val recipeNames = extractRecipeNames(responseText)

            Log.d("AIResponse", "Extracted Recipe Names: $recipeNames")

            // Filter resep berdasarkan nama yang diekstrak
            val filteredRecipes = if (recipeNames.isNotEmpty()) {
                displayedRecipes.filter { recipe ->
                    recipeNames.any { extractedName ->
                        recipe.menuName.contains(extractedName, ignoreCase = true)
                    }
                }
            } else {
                // Jika tidak ada resep yang diekstrak, kembalikan list kosong
                emptyList()
            }

            Log.d("AIResponse", "Filtered Recipes: ${filteredRecipes.map { it.menuName }}")

            filteredRecipes
        } catch (e: Exception) {
            Log.e("AIResponse", "Error parsing AI response", e)
            emptyList()
        }
    }

    private fun extractRecipeNames(responseText: String): List<String> {
        return try {
            // Coba parsing JSON
            val jsonPattern = "\"resep\":\\s*\\[(.*?)\\]".toRegex()
            val jsonMatch = jsonPattern.find(responseText)

            if (jsonMatch != null) {
                // Parsing dari JSON
                jsonMatch.groupValues[1]
                    .split(",")
                    .map { it.trim().trim('"', ' ') }
                    .filter { it.isNotBlank() }
            } else {
                // Parsing dari teks biasa
                val namePattern = "(?:^|[,\\s])([^,\\n]+)( ?=\\s*$|,)".toRegex()
                namePattern.findAll(responseText)
                    .map { it.groupValues[1].trim() }
                    .filter { it.isNotBlank() }
                    .toList()
            }
        } catch (e: Exception) {
            Log.e("AIResponse", "Error extracting recipe names", e)
            emptyList()
        }
    }

    private fun applyFilters() {
        // Jika tidak ada filter spesifik selain "Semua", tampilkan semua resep
        if (selectedFilters.keys.all { it == getString(R.string.filter_all) }) {
            loadMenuData()
            return
        }

        // Ambil filter nutrisi (kecuali "Semua")
        val nutrisiFilters = selectedFilters.filter {
            it.key != getString(R.string.filter_all) &&
                    it.key != "Kadar" &&
                    nutrisiList.contains(it.key)
        }

        // Ambil filter kadar
        val kadarFilter = selectedFilters["Kadar"]

        val prompt = buildFilterPrompt(nutrisiFilters, kadarFilter)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AIFilter", "Prompt: $prompt")

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: ""

                Log.d("AIFilter", "Raw Response: $responseText")

                // Parse dan filter resep berdasarkan respons AI
                val filteredRecipes = parseAIResponse(responseText)

                // Update RecyclerView di thread utama
                requireActivity().runOnUiThread {
                    if (filteredRecipes.isNotEmpty()) {
                        menuAdapter.setData(filteredRecipes)
                    } else {
                        // Jika tidak ada resep yang cocok, tampilkan pesan
                        menuAdapter.setData(emptyList())

                        // Tampilkan dialog atau Toast dengan pesan
                        showNoRelevantRecipesMessage()
                    }
                }
            } catch (e: Exception) {
                // Tangani error
                requireActivity().runOnUiThread {
                    // Kosongkan RecyclerView jika terjadi error
                    menuAdapter.setData(emptyList())

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_message, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("AIFilter", "Error applying filters", e)
                }
            }
        }
    }

    private fun showNoRelevantRecipesMessage() {
        // Buat TextView untuk menampilkan pesan
        val noResultsTextView = view?.findViewById<TextView>(R.id.tv_no_results)

        if (noResultsTextView != null) {
            // Tampilkan TextView
            noResultsTextView.visibility = View.VISIBLE
            noResultsTextView.text = getString(R.string.no_relevant_recipes)

            // Sembunyikan RecyclerView
            recyclerViewMenu.visibility = View.GONE
        } else {
            // Jika tidak ada TextView, gunakan Toast
            Toast.makeText(
                requireContext(),
                getString(R.string.no_relevant_recipes),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadMenuData() {
        // Sembunyikan pesan "Hasil filter tidak ada yang relevan" jika ada
        val noResultsTextView = view?.findViewById<TextView>(R.id.tv_no_results)
        noResultsTextView?.visibility = View.GONE
        recyclerViewMenu.visibility = View.VISIBLE

        val query = database.child("resep")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipes = mutableListOf<MenuModel>()
                for (data in snapshot.children) {
                    val recipe = data.getValue(MenuModel::class.java)
                    recipe?.let { recipes.add(it) }
                }
                menuAdapter.setData(recipes)
                displayedRecipes = recipes // Simpan resep yang ditampilkan
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani error saat memuat data
            }
        })
    }
}