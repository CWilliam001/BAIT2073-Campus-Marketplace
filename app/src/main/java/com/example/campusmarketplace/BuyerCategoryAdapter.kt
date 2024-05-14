package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.BuyerCategoryItemViewBinding
import com.example.campusmarketplace.model.BuyerCategory
import com.squareup.picasso.Picasso

class BuyerCategoryAdapter internal constructor(
    private val context: Context
) : RecyclerView.Adapter<BuyerCategoryAdapter.BuyerCategoryViewHolder>() {
    private var productsCategory = emptyList<BuyerCategory>()

    inner class BuyerCategoryViewHolder(
        private val binding: BuyerCategoryItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(current: BuyerCategory) {
            binding.productCategoryName.text = current.productCategoryName
            Picasso.get().load(current.productCategoryURL).into(binding.productCategoryDisplay)
            // Set click listener for the card view
            binding.buyerCategoryCardView.setOnClickListener {
                // Create a bundle to pass data to the next fragment
                val bundle = Bundle().apply {
                    putString("productCategoryName", current.productCategoryName)
                }
                // Navigate to the buyer product list fragment with the bundle
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_navBuyerCategory_to_navBuyerProductLst, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerCategoryViewHolder {
        val binding = BuyerCategoryItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BuyerCategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productsCategory.size
    }

    override fun onBindViewHolder(holder: BuyerCategoryViewHolder, position: Int) {
        val current = productsCategory[position]
        holder.bind(current)
    }

    fun setProductCategories(categories: List<BuyerCategory>) {
        this.productsCategory = categories
        notifyDataSetChanged() // Notify RecyclerView that data set has changed
    }
}