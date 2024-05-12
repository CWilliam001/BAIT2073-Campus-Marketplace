package com.example.campusmarketplace

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingCartFragment : Fragment() {
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var productAdapter: BuyerCartListAdaptor
    private lateinit var recyclerView: RecyclerView

    private val viewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    private val productViewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }

    private lateinit var sharedPreferences: SharedPreferences
    private var userID: String? = null

    // Define variables to store the checked items and total sales
    private val checkedItems = mutableListOf<SellerProduct>()
    private var totalSales = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.buyerProductLtRecyclerview
        val storageReference = FirebaseStorage.getInstance().reference
        productAdapter = BuyerCartListAdaptor(requireContext(), ::onUpdateProduct, viewModel::deleteCartItem)
        recyclerView.adapter = productAdapter

        // Initialize sharedPreferences and userID in onViewCreated
        sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        userID = sharedPreferences.getString("userID", null)

        // Pass the storageReference to enableSwipeToDelete function
        if (userID != null) {
            productAdapter.enableSwipeToDelete(userID!!, recyclerView, storageReference)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getUserCart(userID.toString())
        viewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            productAdapter.setProducts(productList)
        }

        binding.btnUp.setOnClickListener {
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

        binding.btnBuyNow.setOnClickListener {
            val selectedPaymentMethod = binding.radioPayment.checkedRadioButtonId
            when (selectedPaymentMethod) {
                R.id.radioCard -> {
                    val productList = ArrayList(checkedItems)
                    val bundle = Bundle().apply {
                        putString("userID", userID)
                        putParcelableArrayList("checkedItems", productList)
                    }

                    findNavController().navigate(R.id.action_nav_cart_to_nav_cardPayment, bundle)
                }
                R.id.radioCashDelivery -> {
                    showConfirmationDialog()
                }
            }
        }

    }

    private fun onUpdateProduct(product: SellerProduct) {
        Toast.makeText(requireContext(), "Product ${product.productName} deleted", Toast.LENGTH_SHORT).show()
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Purchase")
            .setMessage("Are you sure you want to proceed with Cash on Delivery?")
            .setPositiveButton("Yes") { dialog, which ->
                for (product in checkedItems) {
                    if (userID != null) {
                        product.paymentMethod = "Cash On Delivery"
                        product.paymentDate = getCurrentTimestamp()
                        product.buyerID = userID.toString()
                        viewModel.deleteCartItem(userID!!, product)
                        productViewModel.updateOrderItem(product)
                    }
                }

                // Clear the checked items list
                checkedItems.clear()

                // Notify the adapter that the dataset has changed
                productAdapter.notifyDataSetChanged()

                Toast.makeText(requireContext(), "Purchase successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_nav_shoppingCart_to_nav_buyerToPickUp)
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
