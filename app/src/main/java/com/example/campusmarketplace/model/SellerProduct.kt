package com.example.campusmarketplace.model

data class SellerProduct(
    var productID:String=" ",
    var productName:String = " ",
    var productDescription:String = " ",
    var productCategory:String = " ",
    var productPrice:String=" ",
    var productCondition:String=" ",
    var productUsageDuration:String=" ",
    var uploadTime:String = " ",
    var sellerID: String = " ",
    //for order part de
    var paymentMethod: String = " ",
    var paymentDate: String = " ",
    var buyerID:String = " ",
    var received:Boolean = false,
    var delivered:Boolean = false,
    var rating:Double = 0.00,

    var productImage: String=" "
)
