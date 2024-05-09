package com.example.campusmarketplace.model

import java.util.Date

data class User (
    val name : String,
    val registerDate : Date = Date(),
    val phoneNumber : String,
    val profileImageUrl : String,
    val address : String,
    val states: String,
    val zipCode: String
)