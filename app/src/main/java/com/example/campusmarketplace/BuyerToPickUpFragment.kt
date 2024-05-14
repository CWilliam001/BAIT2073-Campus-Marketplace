package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerToPickUpBinding

class BuyerToPickUpFragment : Fragment() {
    private lateinit var binding: FragmentBuyerToPickUpBinding
    private lateinit var buyerProductLstAdapter: BuyerOrderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buyerToPickUpViewModel: SellerProductViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerToPickUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCompleted.setOnClickListener {
            findNavController().navigate(R.id.action_nav_pickUp_to_nav_complete)
        }

        binding.btnUp.setOnClickListener {
            findNavController().navigate(R.id.action_nav_pickUp_to_nav_home)
        }


        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        if (userID != null) {
            recyclerView = binding.productUploadRecyclerview
            buyerProductLstAdapter =
                BuyerOrderAdapter(requireContext(), R.id.action_nav_pickUp_to_nav_orderDetail)

            // Set the adapter to the RecyclerView
            recyclerView.adapter = buyerProductLstAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Initialize ViewModel
            buyerToPickUpViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

            // Observe LiveData from ViewModel
            buyerToPickUpViewModel.productLiveData.observe(
                viewLifecycleOwner,
                Observer { products ->
                    // Update RecyclerView adapter with the new list of products
                    buyerProductLstAdapter.setProducts(products)
                })

            // Call function to retrieve user likes
            buyerToPickUpViewModel.retrieveBuyerToPickUpProducts(userID)
        }
    }
}