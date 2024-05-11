package com.example.campusmarketplace

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.BuyerCartListItemViewBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlin.reflect.KFunction2

class BuyerCartListAdaptor internal constructor(
    context: Context,
    private val onUpdateCallBack:(SellerProduct)->Unit,
    private val onDeleteCallback: KFunction2<String, SellerProduct, Unit> // Callback for item removal
):
    RecyclerView.Adapter<BuyerCartListAdaptor.ProductViewHolder>() {
    private var products = emptyList<SellerProduct>() // Cached copy of words
    //1 single item view in a recycler view
    inner class ProductViewHolder(
        private val binding:BuyerCartListItemViewBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(current: SellerProduct){
            binding.productNameDisplay.text = current.productName
            binding.productPriceDisplay.text = current.productPrice
            // Load image using Picasso
            Picasso.get().load(current.productImage).into(binding.productImageDisplay)

            // Handle checkbox click events
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                current.isSelected = isChecked
                // Notify the fragment about the updated product
                onUpdateCallBack.invoke(current)
            }

            binding.productSellerCardView.setOnClickListener {
                // Navigate to the edit page fragment with productId as argument
                val bundle = Bundle().apply {
                    putString("productID", current.productID)
                    putString("productName", current.productName)
                    putString("productDescription", current.productDescription)
                    putString("productCategory", current.productCategory)
                    putString("productPrice", current.productPrice)
                    putString("productCondition", current.productCondition)
                    putString("productUsageDuration", current.productUsageDuration)
                    putString("uploadTime", current.uploadTime)
                    putString("sellerID", current.sellerID)
                    putString("productImage", current.productImage)
                }
                val navController = Navigation.findNavController(binding.root)
                navController.navigate(R.id.action_nav_cart_to_nav_productDetail, bundle)


            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = BuyerCartListItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val current = products[position]
        holder.bind(current)
    }

    override fun getItemCount()= products.size

    internal fun setProducts(products: List<SellerProduct>) {
        this.products = products
        notifyDataSetChanged()
    }

    fun enableSwipeToDelete(userID: String, recyclerView: RecyclerView, storageReference: StorageReference) {
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedProduct = products[position]

                // Show confirmation dialog before deletion
                val builder = AlertDialog.Builder(recyclerView.context)
                builder.setTitle("Deletion Confirmation")
                    .setMessage("Are you sure you want to delete this product from your cart?")
                    .setPositiveButton("Delete") { dialog, which ->
                        // Remove the product from the list
                        val updatedList = products.toMutableList()
                        updatedList.removeAt(position)
                        setProducts(updatedList)

                        // Invoke the onDeleteCallback to handle deletion in ViewModel or repository
                        onDeleteCallback.invoke(userID, deletedProduct)

                        // Remove associated image from Firebase Storage
                        val imageRef = storageReference.child("images/${deletedProduct.productID}.jpg")
                        imageRef.delete()
                            .addOnSuccessListener {
                                Log.d("ProductListAdapter", "Image deleted successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProductListAdapter", "Error deleting image: ${e.message}")
                            }
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // Notify the adapter to refresh the view to cancel the swipe action
                        notifyDataSetChanged()
                    }
                    .setCancelable(false) // Prevent dialog cancellation on outside touch
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

}