package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentShoppingCartBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ShoppingCartFragment : Fragment() {
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var productAdapter: BuyerCartListAdaptor
    private lateinit var recyclerView: RecyclerView // Define recyclerView here

    private val viewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    private lateinit var storageReference: StorageReference // Define storageReference here

    // Define variables to store the checked items and total sales
    private val checkedItems = mutableListOf<SellerProduct>()
    private var totalSales = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentShoppingCartBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        recyclerView = binding.buyerProductLtRecyclerview // Initialize recyclerView from the binding

        // Initialize your storageReference here
        storageReference = FirebaseStorage.getInstance().reference

        productAdapter = BuyerCartListAdaptor(requireContext(), ::onUpdateProduct, viewModel::deleteCartItem)
        recyclerView.adapter = productAdapter

        // Pass the storageReference to enableSwipeToDelete function
        if (userID != null) {
            productAdapter.enableSwipeToDelete(userID, recyclerView, storageReference)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Call retrieveAllItems with sellerID parameter
        viewModel.getUserCart(userID.toString())
        //        viewModel.retrieveAllItems()
        viewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            productAdapter.setProducts(productList)
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }

        // Listen for changes in checked items in the adapter
        productAdapter.setOnItemCheckedListener { product, isChecked ->
            if (isChecked) {
                checkedItems.add(product)
                totalSales += product.productPrice.toDouble()
            } else {
                checkedItems.remove(product)
                totalSales -= product.productPrice.toDouble()
            }

            // Update the total sales TextView
            binding.tvShowTotalSales.text = String.format("RM %.2f", totalSales)

        }
    }

    private fun onUpdateProduct(product: SellerProduct) {
        // Update the UI or perform any other action after the product is deleted
        // For example, you can show a toast message
        Toast.makeText(requireContext(), "Product ${product.productName} deleted", Toast.LENGTH_SHORT).show()
    }
}