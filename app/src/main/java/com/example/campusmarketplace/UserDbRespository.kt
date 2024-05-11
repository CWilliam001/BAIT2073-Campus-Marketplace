package com.example.campusmarketplace

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.concurrent.atomic.AtomicInteger


class UserDbRepository {
    // Get reference to the database
    private val database = FirebaseDatabase.getInstance()

    // Get reference to the product node in the database
    private val userReference = database.getReference("User")
    private val productReference = database.getReference("Product")

    // Buyer Like Data
    fun addProductToLikeList(userId: String, productId: String) {
        val userLikeRef = userReference.child(userId).child("Buy").child("Like")
        // Check if the product ID already exists in the "Like" node
        userLikeRef.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Product ID does not exist in the "Like" node
                    // Add the product to the "Like" list
                    userLikeRef.child(productId).setValue(true)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Insert", "Database query cancelled: ${error.message}")
            }
        })
    }

    fun fetchUserLikes(userId: String, onSuccess: (List<SellerProduct>) -> Unit, onError: (DatabaseError) -> Unit) {
        val userLikesRef = userReference.child(userId).child("Buy").child("Like")

        userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productIds = mutableListOf<String>()
                for (likeSnapshot in snapshot.children) {
                    val productId = likeSnapshot.key
                    productId?.let {
                        productIds.add(it)
                    }
                }
                fetchProductInformation(productIds, onSuccess, onError)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    private fun fetchProductInformation(productIds: List<String>, onSuccess: (List<SellerProduct>) -> Unit, onError: (DatabaseError) -> Unit) {
        val products = mutableListOf<SellerProduct>()
        val counter = AtomicInteger(productIds.size)

        for (productId in productIds) {
            productReference.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val product = snapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        products.add(it)
                    }

                    if (counter.decrementAndGet() == 0) {
                        onSuccess(products)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error)
                }
            })
        }
    }
}



