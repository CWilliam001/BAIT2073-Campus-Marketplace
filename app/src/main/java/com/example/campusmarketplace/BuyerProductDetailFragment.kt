package com.example.campusmarketplace

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentBuyerProductDetailBinding
import com.squareup.picasso.Picasso

class BuyerProductDetailFragment : Fragment() {
    private lateinit var binding: FragmentBuyerProductDetailBinding
    private lateinit var productId: String
    private lateinit var productName: String
    private lateinit var productDescription: String
    private lateinit var productCategory: String
    private lateinit var productPrice: String
    private lateinit var productCondition: String
    private lateinit var productUsageDuration: String
    private  var productImageUri: Uri = Uri.EMPTY // Initialize with an empty Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            productId = bundle.getString("productID", "")
            productName = bundle.getString("productName", "")
            productDescription = bundle.getString("productDescription", "")
            productCategory = bundle.getString("productCategory", "")
            productPrice = bundle.getString("productPrice", "")
            productCondition = bundle.getString("productCondition", "")
            productUsageDuration = bundle.getString("productUsageDuration", "")

            // Retrieve the image URI
            val imageUrl = bundle.getString("productImage", "")
            productImageUri = Uri.parse(imageUrl)
        }

        // set the retrieved values to textView
        // Load and display the image using Picasso or Glide
        Picasso.get().load(productImageUri).into(binding.imgProductImage)
        binding.tvProductName.text = productName
        binding.tvProductPrice.text = String.format("RM %s",productPrice)
        binding.tvCondition.text = productCondition
        binding.tvUsage.text = productUsageDuration
        binding.tvCategory.text= productCategory
        binding.tvDescription.text = productDescription

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }

}