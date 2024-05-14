package com.example.campusmarketplace

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.campusmarketplace.model.BuyerCategory
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class BuyerCategoryRespository {
    private val database = FirebaseDatabase.getInstance()
    private val productCategoryReference = database.getReference("productCategory")
    private val productReference = database.getReference("Product")
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    fun retrieveAllProductItem(liveData: MutableLiveData<List<BuyerCategory>>, sellerID: String) {
        productCategoryReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productCategoryList = mutableListOf<BuyerCategory>()

                for (productSnapshot in snapshot.children) {
                    val productCategory = productSnapshot.getValue(BuyerCategory::class.java)
                    productCategory?.let {
                        // Check if the product category name exists in the sellerProduct database
                        isCategoryNameExistsAndSellerMatch(
                            productCategory.productCategoryName,
                            sellerID
                        ) { exists ->
                            if (exists) {
                                // Fetch image URL from Firebase Storage based on product category URL
                                val productImageRef =
                                    storageReference.child("productCategory/${productCategory.productCategoryID}.png")
                                productImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                    // Update product with image URL
                                    val productCategoryWithImage =
                                        productCategory.copy(productCategoryURL = imageUrl.toString())
                                    productCategoryList.add(productCategoryWithImage)

                                    // Post updated list to LiveData
                                    liveData.postValue(productCategoryList)
                                }.addOnFailureListener { e ->
                                    // Handle image download failure
                                    Log.e(
                                        "RetrieveProducts",
                                        "Failed to download image: ${e.message}"
                                    )
                                    // Notify LiveData with current productCategoryList on failure
                                    liveData.postValue(productCategoryList)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation or errors
                Log.e(
                    "RetrieveProductsCategory",
                    "Database query cancelled or error: ${error.message}"
                )
            }
        })
    }

    private fun isCategoryNameExistsAndSellerMatch(
        categoryName: String,
        sellerID: String,
        callback: (Boolean) -> Unit
    ) {
        productReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExists = false
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    if (product?.productCategory == categoryName && product.sellerID != sellerID && (product.paymentMethod.isNullOrEmpty() || product.paymentMethod.trim() == "")) {
                        isExists = true
                        break
                    }
                }
                callback(isExists) // Call the callback function with the result
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation or errors
                Log.e(
                    "RetrieveProductsCategory",
                    "Database query cancelled or error: ${error.message}"
                )
                callback(false) // Call the callback function with false on cancellation or error
            }
        })
    }
}