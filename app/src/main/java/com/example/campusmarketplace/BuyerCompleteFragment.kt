package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentBuyerCompleteBinding

class BuyerCompleteFragment : Fragment() {

    private lateinit var binding: FragmentBuyerCompleteBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvToPickUp.setOnClickListener{
            findNavController().navigate(R.id.action_nav_complete_to_nav_pickUp)
        }

        binding.btnUp.setOnClickListener {
            findNavController().navigate(R.id.action_nav_complete_to_nav_home)
        }
    }
}