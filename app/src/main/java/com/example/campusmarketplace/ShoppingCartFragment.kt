package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.campusmarketplace.databinding.FragmentShoppingCartBinding

class ShoppingCartFragment : Fragment() {
    private lateinit var binding: FragmentShoppingCartBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }


}