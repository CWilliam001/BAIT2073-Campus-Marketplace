package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerSearchBinding

class BuyerSearchFragment : Fragment() {
    private lateinit var binding: FragmentBuyerSearchBinding
    private lateinit var buyerProductLstAdapter: BuyerProductListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var productViewModel: SellerProductViewModel
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

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
            findNavController().navigate(R.id.action_nav_searchPage_to_main_page)
        }

        // Initialize RecyclerView and Adapter
        recyclerView = binding.productUploadRecyclerview
        buyerProductLstAdapter = BuyerProductListAdapter(
            requireContext(),
            R.id.action_nav_searchPage_to_productDetail,
            userViewModel,
            viewLifecycleOwner
        )
        recyclerView.adapter = buyerProductLstAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

        // Observe LiveData from ViewModel
        productViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
            // Update RecyclerView adapter with the new list of products
            buyerProductLstAdapter.setBuyerProductLst(products)
        })

        binding.searchBtnSearch.setOnClickListener {
            val sharedPreferences =
                requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", null)

            val userInput = binding.searchEtSearchBar.text.toString()
            val productCategory = arguments?.getString("productCategory")
            val productCondition = arguments?.getString("productCondition")
            val productUsageDuration = arguments?.getString("productUsageDuration")

            if ((productCategory.isNullOrEmpty() || productCategory.trim() == "") &&
                (productCondition.isNullOrEmpty() || productCondition.trim() == "") &&
                (productUsageDuration.isNullOrEmpty() || productUsageDuration.trim() == "")
            ) {
                productViewModel.retrieveProductsByProductName(userInput, userID.toString())
            } else {
                productViewModel.retrieveProductFilter(
                    productCategory.toString(),
                    productCondition.toString(),
                    productUsageDuration.toString(),
                    userID.toString()
                )
            }

            // Observe LiveData again after performing the search
            productViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
                // Update RecyclerView adapter with the new list of products
                buyerProductLstAdapter.setBuyerProductLst(products)
                // Notify the adapter that the data set has changed
                buyerProductLstAdapter.notifyDataSetChanged()
            })
        }

        binding.btnFilter.setOnClickListener {
            findNavController().navigate(R.id.action_nav_searchPage_to_search_filter)
        }
    }
}
