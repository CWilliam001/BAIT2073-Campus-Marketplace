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
import androidx.core.view.setMargins
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.DialogRateProductBinding
import com.example.campusmarketplace.databinding.FragmentBuyerBoughtProductDetailsBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BuyerBoughtProductDetailsFragment : Fragment() {
    private lateinit var binding: FragmentBuyerBoughtProductDetailsBinding
    private var productImageUri: Uri = Uri.EMPTY // Initialize with an empty Uri
    private val viewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    private lateinit var product: SellerProduct
    private lateinit var sellerID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerBoughtProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        arguments?.let { bundle ->
            product = bundle.getParcelable("sellerProduct")!!
            product?.let { product ->
                productImageUri = Uri.parse(product.productImage)
            }
        }

        val sellerRatingLiveData = userViewModel.getSellerRating(product.sellerID!!)
        sellerRatingLiveData.observe(viewLifecycleOwner, Observer { averageRating ->
            binding.tvSellerRating.text = String.format("%.2f ⭐️", averageRating)
        })

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(product.sellerID!!)
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val userName = document.getString("name")
                val profileImageUrl = document.getString("profileImageUrl")
                val phoneNumber = document.getString("phoneNumber")
                val location = document.getString("states")

                binding.tvSellerName.text = userName.toString()
                Picasso.get().load(profileImageUrl).transform(RoundedTransformation())
                    .into(binding.imgSellerImage)
                binding.tvSellerPhone.text = phoneNumber.toString()
                binding.tvLocation.text = location.toString()
            }
        }

        if (product.received && product.delivered) {
            binding.btnReceived.visibility = View.GONE
            binding.btnRate.visibility = View.VISIBLE
            binding.tvCompleteTimeLabel.visibility = View.VISIBLE
            binding.tvCompleteTime.visibility = View.VISIBLE
        }

        if (product.rating) {
            binding.btnRate.visibility = View.GONE
            val params = binding.btnChatNow.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = 0
            params.setMargins(70)
            params.width = 0
            binding.btnChatNow.layoutParams = params
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
        sellerID = product.sellerID
        binding.tvCompleteTime.text = product.complete

        binding.btnChatNow.setOnClickListener {
            if (userID != null) {
                val conversationRef = firestore.collection("conversations")

                var matchedSellerID = false
                var conversationID = ""
                Log.d(TAG, "User ID: $userID")
                conversationRef.whereArrayContains("userIDs", userID)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        var index = 0
                        while (index < querySnapshot.documents.size && !matchedSellerID) {
                            val document = querySnapshot.documents[index]
                            val documentID = document.id
                            if (documentID != null) {
                                val otherUserIDs = document.get("userIDs") as List<String>
                                otherUserIDs?.let {
                                    val otherUserID = it.find { it != userID }
                                    if (otherUserID != null) {
                                        if (otherUserID == sellerID) {
                                            matchedSellerID = true
                                            conversationID = documentID
                                        }
                                    }
                                }
                            }
                            index++
                        }

                        Log.d(TAG, "SellerID: $sellerID")
                        Log.d(TAG, "Matched Seller ID = $matchedSellerID")
                        if (matchedSellerID) {
                            Toast.makeText(
                                requireContext(),
                                "Chatting with Existed Seller: $sellerID",
                                Toast.LENGTH_SHORT
                            ).show()
                            val bundle = Bundle()
                            bundle.putSerializable("conversationID", conversationID)
                            findNavController().navigate(
                                R.id.action_nav_buyerOrderDetails_to_nav_chat,
                                bundle
                            )
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Chatting with New Seller: $sellerID",
                                Toast.LENGTH_SHORT
                            ).show()
                            val userIDs = listOf(userID, sellerID)
                            val conversationData = hashMapOf("userIDs" to userIDs)
                            // Create conversation into firestore database
                            conversationRef.add(conversationData)
                                .addOnSuccessListener { documentRef ->
                                    // Conversation document created, navigate to chat screen with conversation ID
                                    val conversationID = documentRef.id

                                    // Create conversation in realtime database
                                    val database = FirebaseDatabase.getInstance()
                                    val chatRef = database.getReference("chats")
                                    chatRef.child(conversationID)
                                        .setValue(hashMapOf("messages" to null))
                                        .addOnSuccessListener {
                                            val bundle = Bundle()
                                            bundle.putSerializable("conversationID", conversationID)
                                            findNavController().navigate(
                                                R.id.action_nav_buyerOrderDetails_to_nav_chat,
                                                bundle
                                            )
                                        }
                                        .addOnFailureListener { e ->
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

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().popBackStack()
        }


        binding.btnReceived.setOnClickListener {
            if (product.received) {
                Toast.makeText(
                    requireContext(),
                    "Product confirmed received",
                    Toast.LENGTH_SHORT * 3
                ).show()
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Confirmation")
                builder.setMessage("Confirm received the product?")
                builder.setPositiveButton("Yes") { _, _ ->
                    if (product.delivered == true) {
                        product.complete = getCurrentTimestamp()
                    }
                    product.received = true
                    viewModel.updateOrderItem(product)

                    Toast.makeText(
                        requireContext(),
                        "Successfully received the product",
                        Toast.LENGTH_SHORT * 3
                    ).show()

                    findNavController().popBackStack()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }

        binding.btnRate.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Rate This Seller")

            // Inflate the layout using data binding
            val dialogLayoutBinding = DialogRateProductBinding.inflate(layoutInflater)
            val ratingBar = dialogLayoutBinding.ratingBar

            builder.setView(dialogLayoutBinding.root)

            builder.setPositiveButton("Submit") { dialog, _ ->
                // Get the user's rating from the RatingBar
                val rating = ratingBar.rating.toInt()
                // Process the rating as needed
                // For example, you can send it to a server or save it locally
                Toast.makeText(requireContext(), "You rated $rating stars", Toast.LENGTH_SHORT)
                    .show()
                userViewModel.addRating(product.sellerID, rating)
                product.rating = true
                viewModel.updateOrderItem(product)
                dialog.dismiss()

                binding.btnRate.visibility = View.GONE
                val params = binding.btnChatNow.layoutParams as ConstraintLayout.LayoutParams
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginStart = 0
                params.setMargins(70)
                params.width = 0
                binding.btnChatNow.layoutParams = params
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}