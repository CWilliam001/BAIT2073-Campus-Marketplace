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

    // Add product to user like list
    fun addProductToLikeList(userId: String, productId: String) {
        repository.addProductToLikeList(userId, productId)
    }

    // Get product from user like list
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

    // Add product to user cart list
    fun addProductToCartList(userId: String, productId: String){
        repository.addProductToCart(userId, productId)
    }

    // Get product from user cart list
    fun getUserCart(userId: String) {
        repository.fetchUserCart(userId,
            onSuccess = { products ->
                _productLiveData.value = products
            },
            onError = { error ->
                // Handle error
            }
        )
    }

    fun deleteCartItem(userId: String, product: SellerProduct) {
        viewModelScope.launch {
            repository.deleteFromCart(userId, product)
        }
    }

}
