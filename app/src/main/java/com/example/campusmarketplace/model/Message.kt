package com.example.campusmarketplace.model

import java.sql.Timestamp
import java.util.UUID

data class Message (
    val id : String = UUID.randomUUID().toString(),
    val senderId : String,
    val conversationId : String,
    val content : String,
    val timestamp: Long = System.currentTimeMillis()
)