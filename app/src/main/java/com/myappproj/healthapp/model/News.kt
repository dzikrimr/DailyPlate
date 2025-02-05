package com.myappproj.healthapp.model

data class News(
    val status: String?,
    val totalResults: Int?,
    val results: List<Article>? // Properti `results` adalah list dari objek `Article`
)

data class Article(
    val article_id: String?, // ID unik artikel
    val title: String?,      // Judul artikel
    val link: String?,       // Link ke artikel
    val keywords: List<String>?, // Kata kunci (bisa `null`)
    val creator: List<String>?,  // Nama pembuat (bisa `null`)
    val video_url: String?,  // URL video jika ada
    val description: String?, // Deskripsi artikel
    val content: String?,     // Konten lengkap artikel
    val pubDate: String?,     // Tanggal publikasi
    val image_url: String?,   // URL gambar artikel
    val source_id: String?,   // ID sumber
    val source_name: String?, // Nama sumber
    val language: String?,    // Bahasa artikel
    val country: List<String>?, // Negara artikel
    val category: List<String>?, // Kategori artikel
    val sentiment: String?    // Sentimen artikel
)
