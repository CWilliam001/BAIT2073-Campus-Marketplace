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
import com.example.campusmarketplace.databinding.SellerProductItemViewBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlin.reflect.KFunction2

class BuyerLikeAdapter internal constructor(
    context: Context,
    private val onDeleteCallback: (String, String) -> Unit, // Callback for item removal
    private val userID: String // UserID to be used in swipe-to-delete
):
    RecyclerView.Adapter<BuyerLikeAdapter.BuyerLikeViewHolder>() {
    private var products = emptyList<SellerProduct>() // Cached copy of words

    inner class BuyerLikeViewHolder(
        private val binding: SellerProductItemViewBinding
    )
        : RecyclerView.ViewHolder(binding.root){

        fun bind(current: SellerProduct){
            binding.productNameDisplay.text = current.productName
            binding.productPriceDisplay.text = String.format("RM %s",current.productPrice)
            // Load image using Picasso
            Picasso.get().load(current.productImage).into(binding.productImageDisplay)

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
                navController.navigate(R.id.action_nav_like_to_nav_productDetail, bundle)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerLikeViewHolder {
        val binding = SellerProductItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return BuyerLikeViewHolder(binding)
    }

    override fun getItemCount()= products.size

    override fun onBindViewHolder(holder: BuyerLikeViewHolder, position: Int) {
        val current = products[position]
        holder.bind(current)
    }

    internal fun setProducts(products: List<SellerProduct>) {
        this.products = products
        notifyDataSetChanged()
    }

    fun enableSwipeToDelete(recyclerView: RecyclerView) {
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

                // Show confirmation dialog
                AlertDialog.Builder(recyclerView.context)
                    .setTitle("Confirmation")
                    .setMessage("Confirm remove this product?")
                    .setPositiveButton("Yes") { dialog, which ->
                        // User confirmed deletion, proceed with deletion
                        val updatedList = products.toMutableList()
                        updatedList.removeAt(position)
                        setProducts(updatedList)
                        onDeleteCallback.invoke(userID, deletedProduct.productID)
                    }
                    .setNegativeButton("No") { dialog, which ->
                        // User canceled deletion, do nothing
                        recyclerView.adapter?.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        // User canceled deletion, do nothing
                        recyclerView.adapter?.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}