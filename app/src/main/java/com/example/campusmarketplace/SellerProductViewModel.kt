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

    fun retrieveAllItems(sellerID: String) {
        repository.retrieveAllProductItem(_productLiveData, sellerID)
    }


//    fun updateItem(product: SellerProduct, imageUri: Uri?) {
//        viewModelScope.launch {
//            repository.update(product,imageUri)
//        }
//    }
    fun updateItem(productId: String, updatesMap: HashMap<String, Any>) {
        repository.update(productId, updatesMap)
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

    fun getProductsByCategory(categoryName: String, sellerID: String) {
        repository.getProductsByCategory(_productLiveData, categoryName,sellerID)
    }

    fun retrieveProductsByUploadTime(sellerID:String) {
        repository.retrieveProductsByUploadTime(_productLiveData,sellerID)
    }

    fun retrieveProductsByProductName(productName:String,sellerID:String) {
        repository.retrieveProductByName(_productLiveData,productName,sellerID)
    }

    fun retrieveProductFilter(productName:String,productCategory:String,productCondition:String,productUsageDuration:String,sellerID:String) {
        repository.retrieveProductFilter(_productLiveData,productName,productCategory,productCondition,productUsageDuration,sellerID)
    }

    fun updateOrderItem(product: SellerProduct) {
        viewModelScope.launch {
            repository.updateOrder(product)
        }
    }

    fun retrieveBuyerToPickUpProducts(buyerID:String){
        repository.retrieveBuyerToPickUpProducts(_productLiveData,buyerID)
    }

    fun retrieveBuyerToCompleteProducts(buyerID:String){
        repository.retrieveBuyerCompleteProducts(_productLiveData,buyerID)
    }

    fun retrieveSellerDeliverProducts(sellerID:String){
        repository.retrieveSellerToDeliverProducts(_productLiveData,sellerID)
    }

    fun retrieveSellerCompleteProducts(sellerID:String){
        repository.retrieveSellerCompleteProducts(_productLiveData,sellerID)
    }

}