package com.example.campusmarketplace

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentSellerToPickUpProductDetailsBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class SellerToPickUpProductDetails : Fragment() {
    private lateinit var binding: FragmentSellerToPickUpProductDetailsBinding
    private var productImageUri: Uri = Uri.EMPTY // Initialize with an empty Uri
    private val viewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }
    private lateinit var product: SellerProduct
    private lateinit var buyerID: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerToPickUpProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)
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
                buyerID = document.id
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

        if (product.delivered) {
            binding.btnCompleted.visibility = View.GONE
            val params = binding.btnChatNow.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.setMargins(70)
            params.width = 0
            binding.btnChatNow.layoutParams = params

            if (product.received) {
                // Additional logic when both delivered and received
                binding.tvCompleteTimeLabel.visibility = View.VISIBLE
                binding.tvCompleteTime.visibility = View.VISIBLE
            }
        } else {
            binding.btnCompleted.visibility = View.VISIBLE
        }

        // set the retrieved values to textView
        // Load and display the image using Picasso or Glide
        Picasso.get().load(productImageUri).into(binding.imgProductImage)
        binding.tvProductName.text = product.productName
        binding.tvProductPrice.text = String.format("RM %.2f", product.productPrice.toDouble())
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

        binding.btnChatNow.setOnClickListener {
            if (userID != null) {
                val firestore = FirebaseFirestore.getInstance()
                val conversationRef = firestore.collection("conversations")

                var matchedBuyerID = false
                var conversationID = ""

                conversationRef.whereArrayContains("userIDs", userID)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        var index = 0
                        while (index < querySnapshot.documents.size && !matchedBuyerID) {
                            val document = querySnapshot.documents[index]
                            val documentID = document.id
                            if (documentID != null) {
                                val otherUserIDs = document.get("userIDs") as List<String>
                                otherUserIDs?.let {
                                    val otherUserID = it.find { it != userID }
                                    if (otherUserID != null) {
                                        if (otherUserID == buyerID) {
                                            matchedBuyerID = true
                                            conversationID = documentID
                                        }
                                    }
                                }
                            }
                            index++
                        }

                        if (matchedBuyerID) {
                            val bundle = Bundle()
                            bundle.putSerializable("conversationID", conversationID)
                            findNavController().navigate(
                                R.id.action_nav_deliver_to_orderDetails_to_nav_chat,
                                bundle
                            )
                        } else {
//                            Log.d(TAG, "Creating new conversation")
                            val userIDs = listOf(userID, buyerID)
                            val conversationData = hashMapOf("userIDs" to userIDs)
                            conversationRef.add(conversationData)
                                .addOnSuccessListener { documentRef ->
                                    val conversationID = documentRef.id
                                    val database = FirebaseDatabase.getInstance()
                                    val chatRef = database.getReference("chats")
                                    chatRef.child(conversationID)
                                        .setValue(hashMapOf("messages" to null))
                                        .addOnSuccessListener {
                                            val bundle = Bundle()
                                            bundle.putSerializable("conversationID", conversationID)
                                            findNavController().navigate(
                                                R.id.action_nav_deliver_to_orderDetails_to_nav_chat,
                                                bundle
                                            )
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Failed to create conversation",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                        }
                    }
            }
        }

        binding.btnCompleted.setOnClickListener {
            if (product.delivered) {
                Toast.makeText(
                    requireContext(),
                    "Product confirmed delivered",
                    Toast.LENGTH_SHORT * 3
                ).show()
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Confirmation")
                builder.setMessage("Confirm delivered the product?")
                builder.setPositiveButton("Yes") { _, _ ->
                    product.delivered = true
                    viewModel.updateOrderItem(product)

                    Toast.makeText(
                        requireContext(),
                        "Successfully delivered the product",
                        Toast.LENGTH_SHORT * 3
                    ).show()

                    findNavController().navigateUp()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }
}