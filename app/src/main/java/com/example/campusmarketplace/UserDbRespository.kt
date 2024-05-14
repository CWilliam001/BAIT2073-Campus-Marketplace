package com.example.campusmarketplace

import android.util.Log
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.atomic.AtomicInteger


class UserDbRepository {
    // Get reference to the database
    private val database = FirebaseDatabase.getInstance()

    // Get reference to the product node in the database
    private val userReference = database.getReference("User")
    private val productReference = database.getReference("Product")
    private val orderReference = database.getReference("Order")

    // Add Product to Buyer Like
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

    // Retrieve Product from Buyer Like
    fun fetchUserLikes(
        userId: String,
        onSuccess: (List<SellerProduct>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
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

    private fun fetchProductInformation(
        productIds: List<String>,
        onSuccess: (List<SellerProduct>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        val products = mutableListOf<SellerProduct>()
        val counter = AtomicInteger(productIds.size)

        for (productId in productIds) {
            productReference.child(productId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
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


    // Add Product to Buyer Cart
    fun addProductToCart(userId: String, productId: String) {
        val userLikeRef = userReference.child(userId).child("Buy").child("Cart")
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

    fun fetchUserCart(
        userId: String,
        onSuccess: (List<SellerProduct>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        val userLikesRef = userReference.child(userId).child("Buy").child("Cart")

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

    fun deleteFromCart(userId: String, product: SellerProduct) {
        userReference.child(userId).child("Buy").child("Cart").child(product.productID)
            .removeValue()
    }

    fun insertRating(userID: String, rating: Number) {
        val ratingRef = userReference.child(userID).child("Sell").child("Rating")

        // Update totalRating and totalNumber
        ratingRef.child("totalRating").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalRating = dataSnapshot.getValue(Int::class.java) ?: 0
                val newTotalRating = totalRating + rating.toInt()
                ratingRef.child("totalRating").setValue(newTotalRating)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        ratingRef.child("totalNumber").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalNumber = dataSnapshot.getValue(Int::class.java) ?: 0
                val newTotalNumber = totalNumber + 1
                ratingRef.child("totalNumber").setValue(newTotalNumber)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    // Function to get the rating for a specific user
    fun getRating(userID: String, callback: (Double) -> Unit) {
        val ratingRef = userReference.child(userID).child("Sell").child("Rating")

        ratingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalRating = dataSnapshot.child("totalRating").getValue(Int::class.java) ?: 0
                val totalNumber = dataSnapshot.child("totalNumber").getValue(Int::class.java) ?: 0

                // Calculate average rating
                val averageRating =
                    if (totalNumber != 0) totalRating.toDouble() / totalNumber else 0.0

                // Invoke callback with the average rating
                callback(averageRating)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                callback(0.0) // Provide default value in case of error
            }
        })
    }

    fun deleteProductFromCart(userId: String, productId: String) {
        val cartRef = userReference.child(userId).child("Buy").child("Cart").child(productId)
        cartRef.removeValue()
    }

    fun deleteFromLove(userId: String, productId: String) {
        val cartRef = userReference.child(userId).child("Buy").child("Like").child(productId)
        cartRef.removeValue()
    }
}