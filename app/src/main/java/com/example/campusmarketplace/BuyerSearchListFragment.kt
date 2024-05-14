package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerSearchBinding
import com.example.campusmarketplace.databinding.FragmentBuyerSearchListBinding


class BuyerSearchListFragment : Fragment() {
    private lateinit var binding: FragmentBuyerSearchListBinding
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
        binding = FragmentBuyerSearchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }

        val productName = arguments?.getString("userInput")
        val productCategory = arguments?.getString("productCategory")
        val productCondition = arguments?.getString("productCondition")
        val productUsageDuration = arguments?.getString("productUsageDuration")
        //val productSellerRating = arguments?.getString("productSellerRating")

        // Initialize RecyclerView and Adapter
        recyclerView = binding.buyerProductLtRecyclerview
        buyerProductLstAdapter = BuyerProductListAdapter(requireContext(), R.id.action_nav_search_product_to_productDetails, userViewModel, viewLifecycleOwner)
        recyclerView.adapter = buyerProductLstAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

        // Observe LiveData from ViewModel
        productViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
            // Update RecyclerView adapter with the new list of products
            buyerProductLstAdapter.setBuyerProductLst(products)
        })

        if ((productCategory.isNullOrEmpty() || productCategory.trim() == "") &&
            (productCondition.isNullOrEmpty() || productCondition.trim() == "") &&
            (productUsageDuration.isNullOrEmpty() || productUsageDuration.trim() == "")
        ) {
            // Call the function from ViewModel to retrieve products
            productViewModel.retrieveProductsByProductName(productName.toString())
        }else{
            productViewModel.retrieveProductFilter(productName.toString(),productCategory.toString(),productCondition.toString(),productUsageDuration.toString())
        }

    }

}