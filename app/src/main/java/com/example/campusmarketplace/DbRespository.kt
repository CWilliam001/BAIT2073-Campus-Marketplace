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
import java.util.Locale

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
                        // Check if paymentMethod is empty or null
                        if (it.paymentMethod.isNullOrEmpty() || it.paymentMethod.trim() == "") {
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
            }
        })
    }

    fun update(productId: String, updatesMap: HashMap<String, Any>) {
        val productRef = productReference.child(productId)
        productRef.updateChildren(updatesMap)
            .addOnSuccessListener {
                Log.d("UpdateProduct", "Product updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("UpdateProduct", "Failed to update product: ${e.message}")
            }
}

    fun delete(product: SellerProduct) {
        productReference.child(product.productID).removeValue()
    }

    fun getProductsByCategory(
        liveData: MutableLiveData<List<SellerProduct>>,
        categoryName: String,
        sellerID: String
    ) {
        val query: Query = productReference.orderByChild("productCategory").equalTo(categoryName)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check if paymentMethod is empty or null
                        if (it.paymentMethod.isNullOrEmpty() || it.paymentMethod.trim() == "") {
                            val productImageRef =
                                storageReference.child("images/${product.productID}.jpg")
                            productImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                val productWithImage =
                                    product.copy(productImage = imageUrl.toString())

                                // Check if sellerID matches
                                if (product.sellerID != sellerID) {
                                    productList.add(productWithImage)
                                    liveData.postValue(productList)
                                }
                            }.addOnFailureListener { e ->
                                Log.e(
                                    "GetProductsByCategory",
                                    "Failed to download image: ${e.message}"
                                )
                            }
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


    fun retrieveProductsByUploadTime(liveData: MutableLiveData<List<SellerProduct>>, sellerID: String) {
        // Query to get products ordered by uploadTime in descending order
        val query = productReference.orderByChild("uploadTime").limitToLast(10)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check if paymentMethod is empty or null
                        if (it.paymentMethod.isNullOrEmpty() || it.paymentMethod.trim() == "") {
                            // Check if the sellerID matches the input sellerID
                            if (it.sellerID != sellerID) {
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
                    }
                }

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


    fun retrieveProductByName(
        liveData: MutableLiveData<List<SellerProduct>>,
        partialProductName: String,
        sellerID: String
    ) {
        // Convert partialProductName to lowercase for consistent case matching
        val partialProductNameLower = partialProductName.lowercase(Locale.ROOT)

        // Query products by partial product name
        productReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        val productName = it.productName?.lowercase(Locale.ROOT) // Convert productName to lowercase if not null

                        // Check if productName contains partialProductNameLower
                        if (productName != null && productName.contains(partialProductNameLower) &&
                            (it.paymentMethod.isNullOrEmpty() || it.paymentMethod.trim() == "") &&
                            it.sellerID != sellerID // Check if sellerID matches input sellerID
                        ) {
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
            }
        })
    }


    fun retrieveProductFilter(
        liveData: MutableLiveData<List<SellerProduct>>,
        productCategory: String?,
        productCondition: String?,
        productUsageDuration: String?,
        sellerID: String // Add sellerID parameter
    ) {
        // Query products by exact matches for parameters
        productReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check exact matches for parameters
                        val categoryMatch = productCategory?.lowercase(Locale.ROOT) == null || it.productCategory?.lowercase(Locale.ROOT) == productCategory?.lowercase(Locale.ROOT)
                        val conditionMatch = productCondition?.lowercase(Locale.ROOT) == null || it.productCondition?.lowercase(Locale.ROOT) == productCondition?.lowercase(Locale.ROOT)
                        val usageDurationMatch = productUsageDuration?.lowercase(Locale.ROOT) == null || it.productUsageDuration?.lowercase(Locale.ROOT) == productUsageDuration?.lowercase(Locale.ROOT)
                        val sellerIDMatch = it.sellerID == sellerID

                        if (categoryMatch && conditionMatch && usageDurationMatch && !sellerIDMatch) {
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
            }
        })
    }



    fun updateOrder(product: SellerProduct) {
        productReference.child(product.productID).setValue(product)
    }


    // Buyer To pick up
    fun retrieveBuyerToPickUpProducts(
        liveData: MutableLiveData<List<SellerProduct>>,
        buyerID: String
    ) {
        val query = productReference
            .orderByChild("buyerID")
            .equalTo(buyerID)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check paymentMethod, received, and delivered status
                        if (!it.paymentMethod.isNullOrEmpty() && (!it.received || !it.delivered)) {
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
            }
        })
    }

    fun retrieveBuyerCompleteProducts(
        liveData: MutableLiveData<List<SellerProduct>>,
        buyerID: String
    ) {
        val query = productReference
            .orderByChild("buyerID")
            .equalTo(buyerID)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check paymentMethod, received, and delivered status
                        if (!it.paymentMethod.isNullOrEmpty() && it.received && it.delivered) {
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
            }
        })
    }

    // Seller To pick up

    fun retrieveSellerCompleteProducts(
        liveData: MutableLiveData<List<SellerProduct>>,
        sellerID: String
    ) {
        val query = productReference
            .orderByChild("sellerID")
            .equalTo(sellerID)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check paymentMethod, received, and delivered status
                        if (it.paymentMethod.trim().isNotEmpty() && it.received && it.delivered) {
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database query cancellation
                Log.e("RetrieveProducts", "Database query cancelled: ${error.message}")
            }
        })
    }

    fun retrieveSellerToDeliverProducts(
        liveData: MutableLiveData<List<SellerProduct>>,
        sellerID: String
    ) {
        val query = productReference.orderByChild("sellerID").equalTo(sellerID)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productListA = mutableListOf<SellerProduct>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(SellerProduct::class.java)
                    product?.let {
                        // Check if buyerID is not null
                        if (it.buyerID.trim().isNotEmpty()) {
                            if (it.paymentMethod.trim().isNotEmpty() && (it.received == false || it.delivered == false)) {
                                // Fetch image URL from Firebase Storage based on product ID
                                val productImageRef = storageReference.child("images/${product.productID}.jpg")
                                productImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                    // Update product with image URL
                                    val productWithImage = product.copy(productImage = imageUrl.toString())
                                    productListA.add(productWithImage)

                                    // Post updated list to LiveData
                                    liveData.postValue(productListA)
                                }.addOnFailureListener { e ->
                                    // Handle image download failure
                                    Log.e("RetrieveProducts", "Failed to download image: ${e.message}")
                                }
                            }
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
}

