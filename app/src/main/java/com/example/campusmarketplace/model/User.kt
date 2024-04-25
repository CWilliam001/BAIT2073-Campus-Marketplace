package com.example.campusmarketplace.model

import java.util.Date

data class User (
    val id : String,
    val email : String,
    val password : String,
    val registerDate : Date,
    var phoneNumber : String,
    var profileImageUrl : String,
    var status : String
) {
    password: String
        get() = this.password
}