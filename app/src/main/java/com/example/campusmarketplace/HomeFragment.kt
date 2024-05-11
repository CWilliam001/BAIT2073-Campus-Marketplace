package com.example.campusmarketplace

import android.content.Context
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var buyerProductLstAdapter: BuyerProductListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var productViewModel: SellerProductViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        if (userID == null) {
            findNavController().navigate(R.id.nav_login)
        } else {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userID!!)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name")
                    binding.buyerTvUsername.text = userName

                    binding.buyerIvCat5.setOnClickListener {
                        findNavController().navigate(R.id.action_nav_home_to_nav_buyerCategory)
                    }

                    binding.buyerIbMyOrder.setOnClickListener {
                        findNavController().navigate(R.id.action_nav_buyer_to_nav_pickUp)
                    }

                    binding.buyerIbMyCart.setOnClickListener{
                        findNavController().navigate(R.id.action_nav_buyer_to_nav_shoppingCart)
                    }

                    binding.buyerIbMyLikes.setOnClickListener{
                        findNavController().navigate(R.id.action_nav_buyer_to_nav_like)
                    }


                    // Initialize RecyclerView and Adapter
                    recyclerView = binding.buyerProductLtRecyclerview
                    buyerProductLstAdapter = BuyerProductListAdapter(requireContext(), R.id.action_nav_buyer_to_nav_productDetail)
                    recyclerView.adapter = buyerProductLstAdapter
                    recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

                    // Initialize ViewModel
                    productViewModel = ViewModelProvider(this).get(SellerProductViewModel::class.java)

                    // Observe LiveData from ViewModel
                    productViewModel.productLiveData.observe(viewLifecycleOwner, Observer { products ->
                        // Update RecyclerView adapter with the new list of products
                        buyerProductLstAdapter.setBuyerProductLst(products)
                    })

                    // Call the function from ViewModel to retrieve products
                    productViewModel.retrieveProductsByUploadTime()

                }
            }
        }
    }
}