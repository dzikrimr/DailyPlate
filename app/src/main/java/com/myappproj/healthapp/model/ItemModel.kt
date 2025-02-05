package com.myappproj.healthapp.model

data class ItemModel(
    val imageURL: String = "",
    val diseases: String? = "",
    val description: String = "",
    val ciri: List<String> = emptyList(),
    val jenis: String = "",
    val tips: List<String> = emptyList()
)