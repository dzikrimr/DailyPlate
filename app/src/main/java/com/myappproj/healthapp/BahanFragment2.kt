package com.myappproj.healthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.myappproj.healthapp.adapter.VerticalBahan
import com.myappproj.healthapp.model.BahanModel

class BahanFragment2 : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VerticalBahan
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bahan2, container, false)
        val textBahan1 = view.findViewById<ImageView>(R.id.back_arrow2)
        recyclerView = view.findViewById(R.id.recyclerbahan_all)
        database = FirebaseDatabase.getInstance().reference.child("bahan")

        // Setting up RecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = VerticalBahan(requireContext())
        recyclerView.adapter = adapter

        // Mengambil data dari Firebase saat fragment dibuat
        fetchDataFromFirebase()

        // Menambahkan event listener untuk tombol back_arrow
        textBahan1.setOnClickListener {
            // Navigasi ke HomeFragment2 saat tombol diklik
            findNavController().navigate(R.id.action_bahanFragment2_to_berandaFragment)
        }

        return view
    }

    /**
     * Method untuk mengambil data dari Firebase Database.
     */
    private fun fetchDataFromFirebase() {
        // Membuat query untuk mengambil data bahan yang memiliki jenis 'tidak'
        val query = database.orderByChild("jenis").equalTo("tidak")

        // Menambahkan event listener ke query untuk mendapatkan data
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Inisialisasi list untuk menyimpan data bahan
                val bahanList = mutableListOf<BahanModel>()
                // Loop melalui setiap child dari snapshot
                for (bahanSnapshot in snapshot.children) {
                    // Mendapatkan nilai bahanModel dari setiap child
                    val bahan = bahanSnapshot.getValue(BahanModel::class.java)
                    // Jika bahan tidak null, tambahkan ke dalam list
                    bahan?.let {
                        bahanList.add(it)
                    }
                }
                // Set data ke adapter untuk ditampilkan di RecyclerView
                adapter.setData(bahanList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan jika query dibatalkan
                // Misalnya, tampilkan pesan kesalahan atau log pesan kesalahan
            }
        })
    }
}
