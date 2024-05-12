package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerSearchBinding

class BuyerSearchFragment : Fragment() {
    private lateinit var binding: FragmentBuyerSearchBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
//        binding.searchBtnSearch.setOnClickListener {
//            val userInput = binding.searchEtSearchBar.text.toString()
//            val productCategory = binding.searchSpCategories.selectedItem.toString()
//            val productCondition = binding.searchSpCondition.selectedItem.toString()
////          val productSellerRating = binding.searchSpCategories.selectedItem.toString()
//            val productUsageDuration = binding.searchSpUsageDuration.selectedItem.toString()
//            val bundle = Bundle()
//            bundle.putString("userInput", userInput)
//            bundle.putString("productCategory", productCategory)
//            bundle.putString("productCondition", productCondition)
//            bundle.putString("productUsageDuration", productUsageDuration)
//            findNavController().navigate(R.id.action_nav_searchPage_to_search_product, bundle)
//        }


    }
}