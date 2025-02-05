package com.myappproj.healthapp.model

data class ChatMessage(
    var text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)