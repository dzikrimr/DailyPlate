package com.myappproj.healthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myappproj.healthapp.model.ItemModel
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class MainKeluhanFragment : Fragment() {

    // Deklarasikan variabel UI
    private lateinit var imgResep: ImageView
    private lateinit var penyakit: TextView
    private lateinit var kelompok: TextView
    private lateinit var deskripsi: TextView
    private lateinit var listCiri: TextView
    private lateinit var listTips: TextView

    // Referensi database
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_keluhan, container, false)
        val textKeluhan = view.findViewById<ImageView>(R.id.back_arrow)

        // Inisialisasi UI
        imgResep = view.findViewById(R.id.img_resep)
        penyakit = view.findViewById(R.id.penyakit)
        kelompok = view.findViewById(R.id.kelompok)
        deskripsi = view.findViewById(R.id.deskripsi)
        listCiri = view.findViewById(R.id.listciri)
        listTips = view.findViewById(R.id.listtips)

        // Inisialisasi referensi database
        database = FirebaseDatabase.getInstance().reference.child("keluhan")

        // Ambil data diseases dari argumen
        val diseases = arguments?.getString("diseases")
        if (diseases != null) {
            fetchDataFromFirebase(diseases)
        }

        textKeluhan.setOnClickListener {
            // Navigasi ke HomeFragment2 saat tombol diklik
            findNavController().navigate(R.id.action_mainKeluhanFragment_to_berandaFragment)
        }

        return view
    }

    private fun fetchDataFromFirebase(diseases: String) {
        database.orderByChild("diseases").equalTo(diseases).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val item = data.getValue(ItemModel::class.java)

                        // Set data ke UI
                        if (item != null) {
                            Glide.with(requireContext())
                                .load(item.imageURL)
                                .placeholder(R.drawable.placeholder_img) // placeholder jika gambar belum dimuat
                                .centerCrop()
                                .transform(
                                    RoundedCornersTransformation(80, 0,
                                        RoundedCornersTransformation.CornerType.TOP)
                                )
                                .into(imgResep)

                            penyakit.text = item.diseases
                            kelompok.text = "Kelompok Penyakit " + item.jenis
                            deskripsi.text = item.description
                            val ciriText = StringBuilder()
                            item.ciri.forEachIndexed { index, ciri ->
                                ciriText.append("• $ciri\n")
                            }
                            listCiri.text = ciriText.toString()

                            val tipsText = StringBuilder()
                            item.tips.forEachIndexed { index, tip ->
                                val tipNumber = index + 1
                                tipsText.append("$tipNumber. $tip\n\n")
                            }
                            listTips.text = tipsText.toString()

                            // Hentikan iterasi setelah menemukan item pertama
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

}
