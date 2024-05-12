package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.BuyerProductLstItemViewBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class BuyerProductListAdapter internal constructor(
    private val context: Context,
    private val destinationId: Int
): RecyclerView.Adapter<BuyerProductListAdapter.BuyerProductListViewHolder>() {
    private var buyerProductLst = emptyList<SellerProduct>()

    inner class BuyerProductListViewHolder(
        private val binding: BuyerProductLstItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(current: SellerProduct) {
            binding.buyerProductLstName.text = current.productName
            binding.buyerProductLstPrice.text = String.format("RM %s",current.productPrice)
            binding.buyerProductLstCondition.text = current.productCondition
            Picasso.get().load(current.productImage).into(binding.buyerProductLstImage)

            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(current.sellerID!!)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name")
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.sellerName.text = userName.toString()
                    Picasso.get().load(profileImageUrl).transform(RoundedTransformation()).into(binding.sellerPicture)
                }
            }

            binding.buyerProductLstCardView.setOnClickListener {
                // Navigate to the product details fragment with productId as argument
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
                navController.navigate(destinationId, bundle)
            }

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