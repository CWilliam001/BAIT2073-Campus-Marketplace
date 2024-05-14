package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentSellerBinding

class SellerFragment : Fragment() {

    private lateinit var binding: FragmentSellerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sellerIbUpload.setOnClickListener {
            //do something here
            findNavController().navigate(R.id.action_nav_seller_to_nav_sellerUploadProduct)
        }
        binding.sellerIbAllproducts.setOnClickListener {
            findNavController().navigate(R.id.action_nav_seller_to_nav_sellerAllProduct)
        }
        binding.sellerIbSales.setOnClickListener {
            findNavController().navigate(R.id.action_nav_seller_to_nav_sellerSalesSummary)
        }

        binding.sellerIbOrders.setOnClickListener {
            findNavController().navigate(R.id.action_nav_seller_to_nav_toDeliver)
        }
    }
}