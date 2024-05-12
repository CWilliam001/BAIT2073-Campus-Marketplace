package com.example.campusmarketplace.model
class Order(
    var userID: String = " ",
    var sellerID: String = " ",
    var productID: String = " ",
    var productName: String = " ",
    var paymentMethod: String = " ",
    var currentDate: String = " ",
    var received: Boolean = false,
    var delivered: Boolean = false
    )