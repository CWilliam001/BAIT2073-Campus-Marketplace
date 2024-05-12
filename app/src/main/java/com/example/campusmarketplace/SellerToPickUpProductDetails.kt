package com.example.campusmarketplace

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentSellerToPickUpProductDetailsBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class SellerToPickUpProductDetails : Fragment() {
    private lateinit var binding: FragmentSellerToPickUpProductDetailsBinding
    private var productImageUri: Uri = Uri.EMPTY // Initialize with an empty Uri
    private val viewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }
    private lateinit var product: SellerProduct
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerToPickUpProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            product = bundle.getParcelable("buyerProduct")!!
            product?.let { product ->
                productImageUri = Uri.parse(product.productImage)
            }
        }

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(product.buyerID!!)
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val userName = document.getString("name")
                val profileImageUrl = document.getString("profileImageUrl")
                val phoneNumber = document.getString("phoneNumber")
                val location = document.getString("states")

                binding.tvBuyerName.text = userName.toString()
                Picasso.get().load(profileImageUrl).transform(RoundedTransformation())
                    .into(binding.imgBuyerImage)
                binding.tvBuyerPhone.text = phoneNumber.toString()
                binding.tvLocation.text = location.toString()
            }
        }

        // set the retrieved values to textView
        // Load and display the image using Picasso or Glide
        Picasso.get().load(productImageUri).into(binding.imgProductImage)
        binding.tvProductName.text = product.productName
        binding.tvProductPrice.text = String.format("RM %s", product.productPrice)
        binding.tvCondition.text = product.productCondition
        binding.tvUsage.text = product.productUsageDuration
        binding.tvCategory.text = product.productCategory
        binding.tvDescription.text = product.productDescription
        binding.tvPayment.text = product.paymentMethod
        binding.tvPaymentTime.text = product.paymentDate

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().popBackStack()
        }

        if(product.received){
            binding.btnCompleted.visibility = View.GONE
            val params = binding.btnChatNow.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = 0
            binding.btnChatNow.layoutParams = params
        }else{
            binding.btnCompleted.visibility = View.VISIBLE
        }

        binding.btnCompleted.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirm")
            builder.setMessage("Confirm delivered the order?")
            builder.setPositiveButton("Yes") { _, _ ->
                product.received = true
                viewModel.updateOrderItem(product)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }
}