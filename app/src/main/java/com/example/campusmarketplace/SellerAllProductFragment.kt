package com.example.campusmarketplace

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
import com.example.campusmarketplace.databinding.FragmentSellerAllProductBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SellerAllProductFragment : Fragment() {
    private lateinit var binding: FragmentSellerAllProductBinding
    private lateinit var productAdapter: SellerProductListAdaptor
    private lateinit var recyclerView: RecyclerView // Define recyclerView here

    private val viewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }

    private lateinit var storageReference: StorageReference // Define storageReference here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSellerAllProductBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.productUploadRecyclerview // Initialize recyclerView from the binding

        // Initialize your storageReference here
        storageReference = FirebaseStorage.getInstance().reference

        productAdapter = SellerProductListAdaptor(requireContext(), ::onUpdateProduct, viewModel::deleteItem)
        recyclerView.adapter = productAdapter

        // Pass the storageReference to enableSwipeToDelete function
        productAdapter.enableSwipeToDelete(recyclerView, storageReference)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.retrieveAllItems()
        viewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            productAdapter.setProducts(productList)
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }

    private fun onUpdateProduct(product: SellerProduct) {
        // Update the UI or perform any other action after the product is deleted
        // For example, you can show a toast message
        Toast.makeText(requireContext(), "Product ${product.productName} deleted", Toast.LENGTH_SHORT).show()
    }
}