package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }

        binding.searchBtnSearchFilter.setOnClickListener {
            val productCategory = binding.searchSpCategories.selectedItem.toString()
            val productCondition = binding.searchSpCondition.selectedItem.toString()
            val productUsageDuration = binding.searchSpUsageDuration.selectedItem.toString()

            val bundle = Bundle()
            bundle.putString("productCategory", productCategory)
            bundle.putString("productCondition", productCondition)
            bundle.putString("productUsageDuration", productUsageDuration)
            findNavController().navigate(R.id.action_nav_filterPage_to_searchPage, bundle)
        }

    }


}