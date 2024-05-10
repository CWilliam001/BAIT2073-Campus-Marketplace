package com.example.campusmarketplace

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusmarketplace.model.SellerProduct
import kotlinx.coroutines.launch

class SellerProductViewModel : ViewModel() {
    private val repository = DbRepository()
    private val _productLiveData = MutableLiveData<List<SellerProduct>>()
    val productLiveData: LiveData<List<SellerProduct>> = _productLiveData

    fun retrieveAllItems() {
        repository.retrieveAllProductItem(_productLiveData)
    }

    fun updateItem(product: SellerProduct, imageUri: Uri?) {
        viewModelScope.launch {
            repository.update(product,imageUri)
        }
    }

    fun insertItem(product: SellerProduct, imageUri: Uri?) {
        viewModelScope.launch {
            repository.insert(product,imageUri)
        }
    }

    fun deleteItem(product: SellerProduct) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }
    fun getProductsByCategory(categoryName: String) {
        repository.getProductsByCategory(_productLiveData, categoryName)
    }

}