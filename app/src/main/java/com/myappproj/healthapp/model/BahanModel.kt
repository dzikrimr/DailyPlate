package com.myappproj.healthapp.model

data class BahanModel(
    val bahanName: String = "",
    val bahanNameEn: String = "", // Nama bahan dalam bahasa Inggris
    val desc: String = "",
    val descEn: String = "", // Deskripsi dalam bahasa Inggris
    val imageURL: String = "",
    val jenis: String = ""
)
