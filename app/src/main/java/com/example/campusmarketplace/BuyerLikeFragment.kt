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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerLikeBinding

class BuyerLikeFragment : Fragment() {
    private lateinit var binding: FragmentBuyerLikeBinding
    private lateinit var buyerProductLstAdapter: BuyerProductListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buyerLikeViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBuyerLikeBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        if (userID != null) {
            recyclerView = binding.buyerProductLtRecyclerview
            buyerProductLstAdapter = BuyerProductListAdapter(requireContext(), R.id.action_nav_like_to_nav_productDetail)

            // Set the adapter to the RecyclerView
            recyclerView.adapter = buyerProductLstAdapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

            // Initialize ViewModel
            buyerLikeViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

            // Observe LiveData from ViewModel
            buyerLikeViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
                // Update RecyclerView adapter with the new list of products
                buyerProductLstAdapter.setBuyerProductLst(products)
            })

            // Call function to retrieve user likes
            buyerLikeViewModel.getUserLikes(userID)
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }
}