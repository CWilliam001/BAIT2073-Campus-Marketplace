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
import com.example.campusmarketplace.databinding.FragmentBuyerLikeBinding

class BuyerLikeFragment : Fragment() {
    private lateinit var binding: FragmentBuyerLikeBinding
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }
    private lateinit var buyerProductLstAdapter: BuyerLikeAdapter
    private lateinit var recyclerView: RecyclerView
    private val buyerLikeViewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBuyerLikeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        if (!userID.isNullOrEmpty()) {
            recyclerView = binding.buyerProductLtRecyclerview
            buyerProductLstAdapter =
                BuyerLikeAdapter(requireContext(), buyerLikeViewModel::deleteFromLove, userID)
            recyclerView.adapter = buyerProductLstAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            buyerProductLstAdapter.enableSwipeToDelete(recyclerView)

            // Observe LiveData from ViewModel
            buyerLikeViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
                // Update RecyclerView adapter with the new list of love products
                buyerProductLstAdapter.setProducts(products)
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