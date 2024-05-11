package com.example.campusmarketplace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.campusmarketplace.model.SellerProduct

class UserViewModel() : ViewModel() {
    private val repository = UserDbRepository()
    private val _productLiveData = MutableLiveData<List<SellerProduct>>()
    val productLiveData: LiveData<List<SellerProduct>> = _productLiveData

    // Method to add a product to the "like" list for the user
    fun addProductToLikeList(userId: String, productId: String) {
        repository.addProductToLikeList(userId, productId)
    }

    fun getUserLikes(userId: String) {
        repository.fetchUserLikes(userId,
            onSuccess = { products ->
                _productLiveData.value = products
            },
            onError = { error ->
                // Handle error
            }
        )
    }
}
