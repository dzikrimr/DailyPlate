package com.myappproj.healthapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MenuModel(
    var menuId: String = "",
    val menuName: String = "",
    val calorieContent: String = "",
    val alat: String = "",
    val imageURL: String = "",
    val diseases: String? = "",
    val bahan: List<String> = emptyList(),
    val langkah: List<String> = emptyList(),
    val menuType: String = "",
    val userId: String = "",
    val username: String = "",
    val likedByUsers: Map<String, Boolean> = mapOf(),
    val likeCount: Int = 0
) : Parcelable

