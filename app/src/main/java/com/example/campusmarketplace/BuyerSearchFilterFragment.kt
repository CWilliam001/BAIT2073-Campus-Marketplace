package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.campusmarketplace.databinding.FragmentAboutUsBinding
import com.example.campusmarketplace.databinding.FragmentBuyerSearchFilterBinding

class BuyerSearchFilterFragment : Fragment() {
    private lateinit var binding: FragmentBuyerSearchFilterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }


}