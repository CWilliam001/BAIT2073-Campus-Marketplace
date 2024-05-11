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
    var productImage: String=" ",
    var isSelected: Boolean = false
)
