package com.example.campusmarketplace.model

import android.os.Parcel
import android.os.Parcelable

data class SellerProduct(
    var productID:String="",
    var productName:String = "",
    var productDescription:String = "",
    var productCategory:String = "",
    var productPrice:String="",
    var productCondition:String="",
    var productUsageDuration:String="",
    var uploadTime:String = "",
    var sellerID: String = "",
    //for order part de
    var paymentMethod: String = "",
    var paymentDate: String = "",
    var buyerID:String = "",
    var received:Boolean = false,
    var delivered:Boolean = false,
    var rating:Double = 0.00,

    var productImage: String=""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productID)
        parcel.writeString(productName)
        parcel.writeString(productDescription)
        parcel.writeString(productCategory)
        parcel.writeString(productPrice)
        parcel.writeString(productCondition)
        parcel.writeString(productUsageDuration)
        parcel.writeString(uploadTime)
        parcel.writeString(sellerID)
        parcel.writeString(paymentMethod)
        parcel.writeString(paymentDate)
        parcel.writeString(buyerID)
        parcel.writeByte(if (received) 1 else 0)
        parcel.writeByte(if (delivered) 1 else 0)
        parcel.writeDouble(rating)
        parcel.writeString(productImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SellerProduct> {
        override fun createFromParcel(parcel: Parcel): SellerProduct {
            return SellerProduct(parcel)
        }

        override fun newArray(size: Int): Array<SellerProduct?> {
            return arrayOfNulls(size)
        }
    }
}
