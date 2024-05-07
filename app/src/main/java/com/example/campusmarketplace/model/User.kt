package com.example.campusmarketplace.model

import java.util.Date

data class User (
    val name : String,
    val registerDate : Date = Date(),
    var phoneNumber : String,
    var profileImageUrl : String,
    var address : String,
    var states: String,
    var zipCode: String
)