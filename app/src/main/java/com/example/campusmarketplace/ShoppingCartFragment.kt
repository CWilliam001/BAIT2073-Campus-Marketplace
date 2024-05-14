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
        initViews()
        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun initViews() {
        recyclerView = binding.buyerProductLtRecyclerview
        sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        userID = sharedPreferences.getString("userID", null)
    }

    private fun setupRecyclerView() {
        productAdapter = BuyerCartListAdaptor(requireContext(), ::onUpdateProduct, viewModel::deleteFromCart, userID ?: "")
        recyclerView.adapter = productAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter.enableSwipeToDelete(recyclerView)
    }

    private fun observeViewModel() {
        userID?.let { viewModel.getUserCart(it) }
        viewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            productAdapter.setProducts(productList)
        }
    }

    private fun setupListeners() {
        binding.btnUp.setOnClickListener {
            findNavController().navigateUp()
        }

        productAdapter.setOnItemCheckedListener { product, isChecked ->
            if (isChecked) {
                checkedItems.add(product)
                totalSales += product.productPrice.toDouble()
            } else {
                checkedItems.remove(product)
                totalSales -= product.productPrice.toDouble()
            }
            updateTotalSales()
        }

        binding.btnBuyNow.setOnClickListener {
            if (checkedItems.isNotEmpty()) {
                // At least one item is checked, proceed with buy now action
                handleBuyNow()
            } else {
                // No item is checked, show a message
                Toast.makeText(requireContext(), "Please select at least one item to buy", Toast.LENGTH_SHORT * 3).show()
            }
        }
    }

    private fun updateTotalSales() {
        binding.tvShowTotalSales.text = String.format("RM %.2f", totalSales)
    }

    private fun handleBuyNow() {
        val selectedPaymentMethod = binding.radioPayment.checkedRadioButtonId
        when (selectedPaymentMethod) {
            R.id.radioCard -> showCardPaymentConfirmationDialog()
            R.id.radioCashDelivery -> showCashDeliveryConfirmationDialog()
        }
    }

    private fun showCardPaymentConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
            .setMessage("Confirm proceed payment with card?")
            .setPositiveButton("Confirm") { _, _ ->
                val productList = ArrayList(checkedItems)
                val bundle = Bundle().apply {
                    putString("userID", userID)
                    putParcelableArrayList("checkedItems", productList)
                }
                totalSales = 0.0
                findNavController().navigate(
                    R.id.action_nav_cart_to_nav_cardPayment,
                    bundle
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showCashDeliveryConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
            .setMessage("Confirm pay at Meetup?")
            .setPositiveButton("Confirm") { _, _ ->
                proceedWithPayment("Pay at Meetup")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun proceedWithPayment(paymentMethod: String) {
        for (product in checkedItems) {
            product.paymentMethod = paymentMethod
            product.paymentDate = getCurrentTimestamp()
            product.buyerID = userID.toString()
            viewModel.deleteCartItem(userID!!, product)
            productViewModel.updateOrderItem(product)
        }
        checkedItems.clear()
        productAdapter.notifyDataSetChanged()
        totalSales = 0.0
        Toast.makeText(requireContext(), "Successfully purchased", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_nav_shoppingCart_to_nav_buyerToPickUp)
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun onUpdateProduct(product: SellerProduct) {
        Toast.makeText(requireContext(), "Product ${product.productName} deleted", Toast.LENGTH_SHORT).show()
    }
}