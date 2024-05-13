package com.example.campusmarketplace.model

import java.util.UUID

data class Chat (
//    var id : String = "",
    val senderId : String = "",
    val content : String = "",
    val datatype : String = "",
    val timestamp : Long = 0
)