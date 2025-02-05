package com.myappproj.healthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.myappproj.healthapp.adapter.MyMenuView
import com.myappproj.healthapp.databinding.FragmentSearchHasilBinding
import com.myappproj.healthapp.model.MenuModel

/**
 * Fragment untuk menampilkan hasil pencarian dari database Firebase.
 */
class SearchHasilFragment : Fragment(), MyMenuView.MenuClickListener {

    private lateinit var binding: FragmentSearchHasilBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var searchResultAdapter: MyMenuView
    private lateinit var backArrow: ImageView
    private val searchResults: MutableList<MenuModel> = mutableListOf()

    /**
     * Metode untuk membuat tampilan fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchHasilBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Metode yang dipanggil setelah tampilan dibuat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseReference = FirebaseDatabase.getInstance().reference.child("resep")

        setupRecyclerView()

        val query = arguments?.getString("query")
        val disease = arguments?.getString("disease")
        val menuType = arguments?.getString("menuType")
        if (!query.isNullOrEmpty()) {
            // Lakukan pencarian dengan query yang diberikan
            performSearch(query, disease, menuType)
        }

        // Dapatkan referensi SearchView dari layout
        val searchView = view.findViewById<SearchView>(R.id.searchview2)

        // Tambahkan listener untuk menangani event pencarian
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Panggil metode performSearch dengan query yang diberikan saat pengguna menekan tombol cari
                performSearch(query, disease, menuType)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Tindakan tambahan yang ingin Anda lakukan saat pengguna mengetik teks pencarian
                return false
            }
        })

        backArrow = view.findViewById(R.id.back_arrow)

        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Metode untuk menyiapkan RecyclerView.
     */
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerViewMenu.layoutManager = layoutManager
        searchResultAdapter = MyMenuView(this) // Inisialisasi adapter dengan MenuClickListener
        binding.recyclerViewMenu.adapter = searchResultAdapter
    }

    /**
     * Metode untuk melakukan pencarian di database Firebase.
     */
    private fun performSearch(query: String, disease: String?, menuType: String?) {
        // Query berdasarkan menuName terlebih dahulu
        val searchQuery = databaseReference.orderByChild("menuName")
            .startAt(query)
            .endAt(query + "\uf8ff")

        searchQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchResults.clear()
                for (menuSnapshot in snapshot.children) {
                    val menu = menuSnapshot.getValue(MenuModel::class.java)
                    menu?.let {
                        it.menuId = menuSnapshot.key ?: "" // Pastikan menuId diisi dari key
                        // Filter penyakit dan tipe menu secara lokal
                        val matchesDisease = disease.isNullOrEmpty() || it.diseases == disease
                        val matchesMenuType = menuType.isNullOrEmpty() || it.menuType == menuType
                        if (matchesDisease && matchesMenuType) {
                            searchResults.add(it)
                        }
                    }
                }
                searchResultAdapter.setData(searchResults)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    /**
     * Metode yang dipanggil ketika item menu diklik.
     */
    override fun onMenuClicked(menuId: String) {
        // Tangani kejadian klik menu di sini
        val action = R.id.mainResepFragment
        val bundle = Bundle()
        bundle.putString("menuId", menuId)
        findNavController().navigate(action, bundle)
    }
}
