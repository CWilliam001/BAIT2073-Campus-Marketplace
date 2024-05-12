package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.SellerProductItemViewBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlin.reflect.KFunction2

class BuyerOrderAdapter internal constructor(
    context: Context,
    private val destinationId: Int
) : RecyclerView.Adapter<BuyerOrderAdapter.BuyerOrderViewHolder>() {
    private var products = emptyList<SellerProduct>()

    inner class BuyerOrderViewHolder(private val binding: SellerProductItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(current: SellerProduct) {
            binding.productNameDisplay.text = current.productName
            binding.productPriceDisplay.text = current.productPrice
            // Load image using Picasso
            Picasso.get().load(current.productImage).into(binding.productImageDisplay)

            binding.productSellerCardView.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelable("sellerProduct", current)
            }
                val navController = Navigation.findNavController(binding.root)
                navController.navigate(destinationId, bundle)

        }
    }
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerOrderViewHolder {
    val binding = SellerProductItemViewBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    )
    return BuyerOrderViewHolder(binding)
}

override fun getItemCount() = products.size

override fun onBindViewHolder(holder: BuyerOrderViewHolder, position: Int) {
    val current = products[position]
    holder.bind(current)
}

internal fun setProducts(products: List<SellerProduct>) {
    this.products = products
    notifyDataSetChanged()
}
}