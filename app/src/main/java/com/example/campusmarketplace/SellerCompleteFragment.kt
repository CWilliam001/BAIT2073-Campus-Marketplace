package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentSellerCompleteBinding

class SellerCompleteFragment : Fragment() {
    private lateinit var binding: FragmentSellerCompleteBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvToPickUp.setOnClickListener{
            findNavController().navigate(R.id.action_nav_sellerComplete_to_nav_toDeliver)
        }

        binding.btnUp.setOnClickListener {

            findNavController().navigate(R.id.action_nav_sellerComplete_to_nav_seller)
        }
    }
}