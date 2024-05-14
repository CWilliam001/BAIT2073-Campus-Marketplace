package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerCategoryBinding

class BuyerCategoryFragment : Fragment() {
    private lateinit var binding: FragmentBuyerCategoryBinding
    private lateinit var buyerCategoryAdapter: BuyerCategoryAdapter
    private lateinit var recyclerView: RecyclerView

    private val buyerCategoryViewModel: BuyerCategoryViewModel by lazy {
        ViewModelProvider(this).get(BuyerCategoryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBuyerCategoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.BuyerCategoryRecyclerview
        buyerCategoryAdapter = BuyerCategoryAdapter(requireContext())
        recyclerView.adapter = buyerCategoryAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)


        buyerCategoryViewModel.retrieveAllItems(userID.toString())
        buyerCategoryViewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            buyerCategoryAdapter.setProductCategories(productList)
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }
}