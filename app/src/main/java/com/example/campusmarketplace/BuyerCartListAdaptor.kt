package com.example.campusmarketplace

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private val onUpdateCallBack: (SellerProduct) -> Unit,
    private val onDeleteCallback: (String, String) -> Unit, // Callback for item removal
    private val userID: String // UserID to be used in swipe-to-delete
) :
    RecyclerView.Adapter<BuyerCartListAdaptor.ProductViewHolder>() {
    private var products = emptyList<SellerProduct>() // Cached copy of words

    // Define a listener to communicate checkbox state changes to the fragment
    private var onItemCheckedListener: ((SellerProduct, Boolean) -> Unit)? = null

    fun setOnItemCheckedListener(listener: (SellerProduct, Boolean) -> Unit) {
        onItemCheckedListener = listener
    }

    inner class ProductViewHolder(
        private val binding: BuyerCartListItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(current: SellerProduct) {
            binding.productNameDisplay.text = current.productName
            binding.productPriceDisplay.text = String.format("RM %.2f", current.productPrice.toDouble())
            // Load image using Picasso
            Picasso.get().load(current.productImage).into(binding.productImageDisplay)
            if(current.paymentMethod.isNotEmpty() && current.paymentMethod.trim() != ""){
                binding.checkBox.visibility = View.GONE
                binding.tvSold.visibility = View.VISIBLE
            }

            if (current.paymentMethod.isNullOrEmpty() || current.paymentMethod.trim() == "") {
                // If not bought, enable the checkbox and set the listener
                binding.checkBox.isEnabled = true
                binding.checkBox.isChecked = false // Ensure the checkbox is unchecked
                binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                    // Invoke the listener passing the current product and checkbox state
                    onItemCheckedListener?.invoke(current, isChecked)
                }
            } else {
                binding.checkBox.isChecked = false
                binding.checkBox.isEnabled = false
                binding.checkBox.setOnCheckedChangeListener(null) // Remove any previous listener
            }

            binding.productSellerCardView.setOnClickListener {
                if (!current.paymentMethod.isNullOrEmpty() && current.paymentMethod.trim() != "") {
                    // Show a toast message indicating that the product is sold
                    Toast.makeText(binding.root.context, "Product sold", Toast.LENGTH_SHORT).show()
                } else {
                    // Navigate to the edit page fragment with productId as argument
                    val bundle = Bundle().apply {
                        putParcelable("buyerProduct", current)
                    }
                    val navController = Navigation.findNavController(binding.root)
                    navController.navigate(R.id.action_nav_cart_to_nav_productDetail, bundle)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = BuyerCartListItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val current = products[position]
        holder.bind(current)
    }

    override fun getItemCount() = products.size

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
                        // User confirmed deletion, remove the product from the list
                        val updatedList = products.toMutableList()
                        updatedList.removeAt(position)
                        setProducts(updatedList)
                        // Invoke the onDeleteCallback to handle deletion in ViewModel or repository
                        onDeleteCallback.invoke(userID, deletedProduct.productID)

                    }
                    .setNegativeButton("No") { dialog, which ->
                        // User canceled deletion, notify adapter to refresh item
                        recyclerView.adapter?.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}