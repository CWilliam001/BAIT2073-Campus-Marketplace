package com.example.campusmarketplace

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerProductListBinding

class BuyerProductListFragment : Fragment() {
    private lateinit var binding: FragmentBuyerProductListBinding
    private lateinit var buyerProductLstAdapter: BuyerProductListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var productViewModel: SellerProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBuyerProductListBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.buyerProductLtRecyclerview
        buyerProductLstAdapter = BuyerProductListAdapter(requireContext(), R.id.action_navBuyerProductList_to_navBuyerProductDetail)
        recyclerView.adapter = buyerProductLstAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

        // Retrieve category name passed from previous fragment
        val categoryName = arguments?.getString("productCategoryName")

        productViewModel.productLiveData.observe(viewLifecycleOwner, { productList ->
            categoryName?.let { category ->
                val filteredList = productList.filter { it.productCategory == category }
                buyerProductLstAdapter.setBuyerProductLst(filteredList)

//                if (buyerProductLstAdapter.itemCount == 0) {
//                    Toast.makeText(requireContext(), "No products available in this category", Toast.LENGTH_SHORT).show()
//                    Log.d("BuyerProductListAdapter", "No products available in this category")
//                } else {
//                    Toast.makeText(requireContext(), "Products loaded successfully", Toast.LENGTH_SHORT).show()
//                }


            }
        })

        // Fetch products for the selected category
        categoryName?.let {
            productViewModel.getProductsByCategory(it)
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }

}