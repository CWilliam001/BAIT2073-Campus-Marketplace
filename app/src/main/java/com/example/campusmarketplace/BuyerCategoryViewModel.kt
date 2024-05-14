package com.example.campusmarketplace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.campusmarketplace.model.BuyerCategory

class BuyerCategoryViewModel : ViewModel() {
    private val repository = BuyerCategoryRespository()
    private val _productCategoryLiveData = MutableLiveData<List<BuyerCategory>>()
    val productLiveData: LiveData<List<BuyerCategory>> = _productCategoryLiveData

    fun retrieveAllItems(sellerID: String) {
        repository.retrieveAllProductItem(_productCategoryLiveData, sellerID)
    }
}