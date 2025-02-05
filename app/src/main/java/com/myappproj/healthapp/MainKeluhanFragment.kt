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
import java.util.Locale

class MainKeluhanFragment : Fragment() {

    private lateinit var imgResep: ImageView
    private lateinit var penyakit: TextView
    private lateinit var kelompok: TextView
    private lateinit var deskripsi: TextView
    private lateinit var listCiri: TextView
    private lateinit var listTips: TextView

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_keluhan, container, false)
        val textKeluhan = view.findViewById<ImageView>(R.id.back_arrow)

        imgResep = view.findViewById(R.id.img_resep)
        penyakit = view.findViewById(R.id.penyakit)
        kelompok = view.findViewById(R.id.kelompok)
        deskripsi = view.findViewById(R.id.deskripsi)
        listCiri = view.findViewById(R.id.listciri)
        listTips = view.findViewById(R.id.listtips)

        database = FirebaseDatabase.getInstance().reference.child("keluhan")

        val diseases = arguments?.getString("diseases")
        if (diseases != null) {
            fetchDataFromFirebase(diseases)
        }

        textKeluhan.setOnClickListener {
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

                        if (item != null) {
                            val currentLanguage = Locale.getDefault().language
                            val isEnglish = currentLanguage == "en"

                            val diseaseText = if (isEnglish) item.diseasesEn else item.diseases
                            val descriptionText = if (isEnglish) item.descriptionEn else item.description
                            val ciriList = if (isEnglish) item.ciriEn else item.ciri
                            val tipsList = if (isEnglish) item.tipsEn else item.tips

                            // Tentukan teks kelompok berdasarkan bahasa dan jenis penyakit
                            val kelompokText = if (isEnglish) {
                                when (item.jenis.lowercase()) {
                                    "dalam" -> "Internal Disease"
                                    "luar" -> "External Disease"
                                    else -> "Disease Group"
                                }
                            } else {
                                "Kelompok Penyakit ${item.jenis}"
                            }

                            Glide.with(requireContext())
                                .load(item.imageURL)
                                .placeholder(R.drawable.placeholder_img)
                                .centerCrop()
                                .transform(
                                    RoundedCornersTransformation(
                                        80, 0, RoundedCornersTransformation.CornerType.TOP
                                    )
                                )
                                .into(imgResep)

                            penyakit.text = diseaseText
                            kelompok.text = kelompokText
                            deskripsi.text = descriptionText

                            val ciriText = ciriList.joinToString("\n") { "• $it" }
                            listCiri.text = ciriText

                            val tipsText = tipsList.mapIndexed { index, tip -> "${index + 1}. $tip" }
                                .joinToString("\n\n")
                            listTips.text = tipsText

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
