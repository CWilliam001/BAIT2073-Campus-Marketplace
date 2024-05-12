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
import com.example.campusmarketplace.databinding.FragmentSellerCompleteBinding

class SellerCompleteFragment : Fragment() {
    private lateinit var binding: FragmentSellerCompleteBinding
    private lateinit var buyerProductLstAdapter: SellerOrderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sellerCompleteViewModel: SellerProductViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvToPickUp.setOnClickListener{
            findNavController().navigate(R.id.action_nav_sellerComplete_to_nav_toDeliver)
        }

        binding.btnUp.setOnClickListener {

            findNavController().navigate(R.id.action_nav_sellerComplete_to_nav_seller)
        }

        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        if (userID != null) {
            recyclerView = binding.productUploadRecyclerview
            buyerProductLstAdapter = SellerOrderAdapter(requireContext(), R.id.action_nav_sellerComplete_to_nav_orderDetails)

            // Set the adapter to the RecyclerView
            recyclerView.adapter = buyerProductLstAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Initialize ViewModel
            sellerCompleteViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

            // Observe LiveData from ViewModel
            sellerCompleteViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
                // Update RecyclerView adapter with the new list of products
                buyerProductLstAdapter.setProducts(products)
            })

            // Call function to retrieve user likes
            sellerCompleteViewModel.retrieveSellerCompleteProducts(userID)


        }
    }
}