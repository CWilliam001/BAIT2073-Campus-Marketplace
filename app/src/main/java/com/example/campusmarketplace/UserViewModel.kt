package com.example.campusmarketplace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusmarketplace.model.SellerProduct
import kotlinx.coroutines.launch

class UserViewModel() : ViewModel() {
    private val repository = UserDbRepository()
    private val _productLiveData = MutableLiveData<List<SellerProduct>>()
    val productLiveData: LiveData<List<SellerProduct>> = _productLiveData

    // Map to hold LiveData for each seller's rating
    private val ratingMap = mutableMapOf<String, MutableLiveData<Double>>()

    // Add product to user like list
    fun addProductToLikeList(userId: String, productId: String) {
        repository.addProductToLikeList(userId, productId)
    }

    // Get product from user like list
    fun getUserLikes(userId: String) {
        repository.fetchUserLikes(userId, onSuccess = { products ->
            _productLiveData.value = products
        }, onError = { error ->
            // Handle error
        })
    }

    // Add product to user cart list
    fun addProductToCartList(userId: String, productId: String) {
        repository.addProductToCart(userId, productId)
    }

    // Get product from user cart list
    fun getUserCart(userId: String) {
        repository.fetchUserCart(userId, onSuccess = { products ->
            _productLiveData.value = products
        }, onError = { error ->
            // Handle error
        })
    }

    fun deleteCartItem(userId: String, product: SellerProduct) {
        viewModelScope.launch {
            repository.deleteFromCart(userId, product)
        }
    }

    fun addRating(userID: String, rating: Number) {
        repository.insertRating(userID, rating)
    }

    // Function to get LiveData for a specific seller's rating
    fun getSellerRating(userID: String): LiveData<Double> {
        // Check if LiveData for this userID already exists
        if (!ratingMap.containsKey(userID)) {
            val ratingLiveData = MutableLiveData<Double>()
            ratingMap[userID] = ratingLiveData

            // Fetch rating from repository and post to LiveData
            repository.getRating(userID) { averageRating ->
                ratingLiveData.postValue(averageRating)
            }
        }
        return ratingMap[userID]!!
    }

    fun deleteFromCart(userId: String, productId: String) {
        viewModelScope.launch {
            repository.deleteProductFromCart(userId, productId)
        }
    }

    fun deleteFromLove(userId: String, productId: String) {
        viewModelScope.launch {
            repository.deleteFromLove(userId, productId)
        }
    }

    fun getTotalCompleteSales(sellerID: String, callback: (Int) -> Unit) {
        repository.countTotalCompleteSales(sellerID, callback)
    }

    fun getTotalProcessingSales(sellerID: String, callback: (Int) -> Unit) {
        repository.countTotalProcessingSales(sellerID, callback)
    }

    fun getTotalSales(sellerID: String, callback: (Double) -> Unit) {
        repository.sumTotalCompleteSalesPrice(sellerID, callback)
    }
}