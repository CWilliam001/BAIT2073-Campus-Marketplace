package com.example.campusmarketplace.model

data class Conversation (
    val id : String,
    val name : String,
    val members : List<String>,
    val description : String? = null,
    val imageUrl : String? = null
)