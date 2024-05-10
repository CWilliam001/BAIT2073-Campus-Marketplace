package com.example.campusmarketplace

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.BuyerProductLstItemViewBinding
import com.example.campusmarketplace.model.SellerProduct
import com.squareup.picasso.Picasso

class BuyerProductListAdapter internal constructor(
    private val context: Context
): RecyclerView.Adapter<BuyerProductListAdapter.BuyerProductListViewHolder>() {
    private var buyerProductLst = emptyList<SellerProduct>()

    inner class BuyerProductListViewHolder(
        private val binding: BuyerProductLstItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(current: SellerProduct) {
            binding.buyerProductLstName.text = current.productName
            binding.buyerProductLstPrice.text = current.productPrice
            binding.buyerProductLstCondition.text = current.productCondition
            Picasso.get().load(current.productImage).into(binding.buyerProductLstImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerProductListViewHolder {
        val binding = BuyerProductLstItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BuyerProductListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return buyerProductLst.size
    }

    override fun onBindViewHolder(holder: BuyerProductListViewHolder, position: Int) {
        val current = buyerProductLst[position]
        holder.bind(current)
    }

    fun setBuyerProductLst(categories: List<SellerProduct>) {
        this.buyerProductLst = categories
        notifyDataSetChanged() // Notify RecyclerView that data set has changed
    }
}