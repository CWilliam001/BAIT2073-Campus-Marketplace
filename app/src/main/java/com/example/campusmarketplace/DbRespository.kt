package com.example.campusmarketplace

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DbRepository {
    // Get reference to the database
    private val database = FirebaseDatabase.getInstance()
    // Get reference to the product node in the database
    private val productReference = database.getReference("Product")

    // Reference to Firebase Storage
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    fun insert(product: SellerProduct, imageUri: Uri?) {
        val query: Query = productReference.orderByChild("productID").equalTo(product.productID)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) { // If product with the same name doesn't exist, only insert
                    val productKey = productReference.push().key ?: return

                    // Upload image to Firebase Storage if an image URI is provided
                    imageUri?.let { uri ->
                        val imageRef = storageReference.child("images/$productKey.jpg")
                        imageRef.putFile(uri)
                            .addOnSuccessListener { // Image uploaded successfully
                                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                    product.productID = productKey
                                    product.productImage = imageUrl.toString() // Save image URL in product
                                    productReference.child(productKey).setValue(product)
                                }
                            }
                    } ?: run {
                        // If no image provided, only save product data
                        product.productID = productKey
                        productReference.child(productKey).setValue(product)

                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Insert", "Database query cancelled: ${error.message}")
            }
        })
    }

fun retrieveAllProductItem(
    liveData: MutableLiveData<List<SellerProduct>>,
    sellerID: String
) {
    val query = productReference.orderByChild("sellerID").equalTo(sellerID)

    query.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productList = mutableListOf<SellerProduct>()

            for (productSnapshot in snapshot.children) {
                val product = productSnapshot.getValue(SellerProduct::class.java)
                product?.let {
                    // Fetch image URL from Firebase Storage based on product ID
                    val productImageRef = storageReference.child("images/${product.productID}.jpg")
                    productImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        // Update product with image URL
                        val productWithImage = product.copy(productImage = imageUrl.toString())
                        productList.add(productWithImage)

                        // Post updated list to LiveData
                        liveData.postValue(productList)
                    }.addOnFailureListener { e ->
                        // Handle image download failure
                        Log.e("RetrieveProducts", "Failed to download image: ${e.message}")
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle database query cancellation
            Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
        }
    })
}

    fun update(product: SellerProduct, imageUri: Uri?) {
        val productKey = product.productID
//        val productRef = productReference.child(productKey)

        // Check if imageUri is provided and if it's different from the current product image
        val isNewImage = imageUri != null && product.productImage != imageUri.toString()

        if (isNewImage) {
            val newImageRef = storageReference.child("images/$productKey.jpg")
            val oldImageRef = storageReference.child("images/$productKey.jpg")

            // Delete old image before uploading the new one
            oldImageRef.delete().addOnSuccessListener {
                // Upload new image
                newImageRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        newImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            product.productImage = imageUrl.toString() // Update product with new image URL
                            productReference.child(product.productID).setValue(product)

                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UpdateProduct", "Failed to upload new image: ${e.message}")
                    }
            }.addOnFailureListener { e ->
                Log.e("UpdateProduct", "Failed to delete old image: ${e.message}")
            }
        } else {
            // If no new image provided or image remains the same, update other product data only
            productReference.child(product.productID).setValue(product)
        }
    }

    fun delete(product: SellerProduct) {
        productReference.child(product.productID).removeValue()
    }

    fun getProductsByCategory(
        liveData: MutableLiveData<List<SellerProduct>>,
        categoryName: String
    ) {
        val query: Query = productReference.orderByChild("productCategory").equalTo(categoryName)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        val productImageRef =
                            storageReference.child("images/${product.productID}.jpg")
                        productImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            val productWithImage =
                                product.copy(productImage = imageUrl.toString())
                            productList.add(productWithImage)
                            liveData.postValue(productList)
                        }.addOnFailureListener { e ->
                            Log.e(
                                "GetProductsByCategory",
                                "Failed to download image: ${e.message}"
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "GetProductsByCategory",
                    "Database query cancelled: ${error.message}"
                )
            }
        })
    }

    fun retrieveProductsByUploadTime(liveData: MutableLiveData<List<SellerProduct>>) {
        // Query to get products ordered by uploadTime in descending order
        val query = productReference.orderByChild("uploadTime").limitToLast(10)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Fetch image URL from Firebase Storage based on product ID
                        val productImageRef = storageReference.child("images/${product.productID}.jpg")
                        productImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            // Update product with image URL
                            val productWithImage = product.copy(productImage = imageUrl.toString())
                            productList.add(productWithImage)

                            // Sort the list by upload time in descending order
                            productList.sortByDescending { it.uploadTime }

                            // Post sorted list to LiveData
                            liveData.postValue(productList.toList()) // Ensure immutability of the list
                        }.addOnFailureListener { e ->
                            // Handle image download failure
                            // You can add logging or error handling here
                        }
                    }
                }

                // Move sorting and posting outside the loop for efficiency
                // Sort the list by upload time in descending order
                productList.sortByDescending { it.uploadTime }

                // Post sorted list to LiveData
                liveData.postValue(productList.toList()) // Ensure immutability of the list
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                // You can add logging or error handling here
            }
        })
    }

}