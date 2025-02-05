package com.myappproj.healthapp.model

data class ItemModel(
    val imageURL: String = "",
    val diseases: String? = "",
    val diseasesEn: String? = "", // Nama penyakit dalam bahasa Inggris
    val description: String = "",
    val descriptionEn: String = "", // Deskripsi dalam bahasa Inggris
    val ciri: List<String> = emptyList(),
    val ciriEn: List<String> = emptyList(), // Ciri-ciri dalam bahasa Inggris
    val jenis: String = "",
    val tips: List<String> = emptyList(),
    val tipsEn: List<String> = emptyList() // Tips dalam bahasa Inggris
)
