package com.myappproj.healthapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myappproj.healthapp.adapter.MyMenuView
import com.myappproj.healthapp.model.MenuModel

class CategoryResepFragment : Fragment(), MyMenuView.MenuClickListener {

    private lateinit var adapter: MyMenuView
    private lateinit var recyclerView: RecyclerView
    private lateinit var backArrow: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menginisialisasi layout untuk fragment ini
        val view = inflater.inflate(R.layout.fragment_category_resep, container, false)

        // Inisialisasi RecyclerView dan adapter
        recyclerView = view.findViewById(R.id.recyclerview_mymenu)
        adapter = MyMenuView(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Mendapatkan data dari Firebase
        retrieveDataFromFirebase()

        // Inisialisasi tombol panah kembali dan set listener untuk kembali
        backArrow = view.findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    /**
     * Fungsi callback saat suatu menu diklik di RecyclerView.
     * Mengarahkan ke MainResepFragment dan meneruskan nama menu sebagai argumen.
     */
    override fun onMenuClicked(menuId: String) {
        val bundle = Bundle().apply {
            putString("menuId", menuId)
        }
        findNavController().navigate(R.id.mainResepFragment, bundle)
    }

    /**
     * Mengambil data dari Firebase Realtime Database dan mengisi RecyclerView dengan item-menu.
     */
    private fun retrieveDataFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("resep")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Inisialisasi list untuk menyimpan item-menu
                val menuList = mutableListOf<MenuModel>()

                // Iterasi melalui setiap child node di dataSnapshot
                for (menuSnapshot in snapshot.children) {
                    // Deserialize setiap item-menu dari snapshot
                    val menu = menuSnapshot.getValue(MenuModel::class.java)
                    // Jika menu tidak null, tambahkan ke dalam menuList
                    menu?.let {
                        menuList.add(it)
                    }
                }

                // Set data ke adapter untuk ditampilkan di RecyclerView
                adapter.setData(menuList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan yang terjadi selama pengambilan data
                Log.e(TAG, "Gagal membaca nilai.", error.toException())
            }
        })
    }
}
