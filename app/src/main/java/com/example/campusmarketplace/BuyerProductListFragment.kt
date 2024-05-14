package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerProductListBinding

class BuyerProductListFragment : Fragment() {
    private lateinit var binding: FragmentBuyerProductListBinding
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
        // Inflate the layout for this fragment
        binding = FragmentBuyerProductListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        recyclerView = binding.buyerProductLtRecyclerview
        buyerProductLstAdapter = BuyerProductListAdapter(
            requireContext(),
            R.id.action_navBuyerProductList_to_navBuyerProductDetail,
            userViewModel,
            viewLifecycleOwner
        )
        recyclerView.adapter = buyerProductLstAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.buyerProductLtRecyclerview.isVisible = false
        binding.tvEmpty.visibility = View.VISIBLE

        // Initialize ViewModel
        productViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

        // Retrieve category name passed from previous fragment
        val categoryName = arguments?.getString("productCategoryName")

        productViewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            categoryName?.let { category ->
                val filteredList = productList.filter { it.productCategory == category }
                buyerProductLstAdapter.setBuyerProductLst(filteredList)

                if (buyerProductLstAdapter.itemCount != 0) {
                    binding.buyerProductLtRecyclerview.isVisible = true
                    binding.tvEmpty.visibility = View.GONE
                }
            }
        }

        // Fetch products for the selected category and seller ID
        categoryName?.let {
            userID?.let { seller ->
                productViewModel.getProductsByCategory(it, seller)
            }
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }
}