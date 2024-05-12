package com.example.campusmarketplace

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentBuyerProductDetailBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class BuyerProductDetailFragment : Fragment() {
    private lateinit var binding: FragmentBuyerProductDetailBinding
    private lateinit var productId: String
    private lateinit var productName: String
    private lateinit var productDescription: String
    private lateinit var productCategory: String
    private lateinit var productPrice: String
    private lateinit var productCondition: String
    private lateinit var productUsageDuration: String
    private lateinit var uploadTime: String
    private lateinit var sellerID: String
    private var productImageUri: Uri = Uri.EMPTY // Initialize with an empty Uri

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyerProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        arguments?.let { bundle ->
            productId = bundle.getString("productID", "")
            productName = bundle.getString("productName", "")
            productDescription = bundle.getString("productDescription", "")
            productCategory = bundle.getString("productCategory", "")
            productPrice = bundle.getString("productPrice", "")
            productCondition = bundle.getString("productCondition", "")
            productUsageDuration = bundle.getString("productUsageDuration", "")
            uploadTime = bundle.getString("uploadTime", "")
            sellerID = bundle.getString("sellerID", " ")

            // Retrieve the image URI
            val imageUrl = bundle.getString("productImage", "")
            productImageUri = Uri.parse(imageUrl)
        }

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(sellerID!!)
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

        // set the retrieved values to textView
        // Load and display the image using Picasso or Glide
        Picasso.get().load(productImageUri).into(binding.imgProductImage)
        binding.tvProductName.text = productName
        binding.tvProductPrice.text = String.format("RM %s", productPrice)
        binding.tvCondition.text = productCondition
        binding.tvUsage.text = productUsageDuration
        binding.tvCategory.text = productCategory
        binding.tvDescription.text = productDescription
        binding.tvUploadTime.text = uploadTime

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }

        binding.btnLike.setOnClickListener {
            if (userID != null) {
                userViewModel.addProductToLikeList(userID, productId)
                Toast.makeText(
                    requireContext(),
                    "Product already added to like list",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnChatSeller.setOnClickListener {

            if (userID != null) {
                val firestore = FirebaseFirestore.getInstance()
                val conversationRef = firestore.collection("conversations")

                var matchedSellerID = false
                var conversationID = ""
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

                        if (matchedSellerID) {
//                            Toast.makeText(requireContext(), "Chatting with Existed Seller: $sellerID", Toast.LENGTH_SHORT).show()
                            val bundle = Bundle()
                            bundle.putSerializable("conversationID", conversationID)
                            findNavController().navigate(R.id.action_nav_buyerProductDetail_to_nav_chat, bundle)
                        } else {
//                            Toast.makeText(requireContext(), "Chatting with New Seller: $sellerID", Toast.LENGTH_SHORT).show()
                            val userIDs = listOf(userID, sellerID)
                            val conversationData = hashMapOf("userIDs" to userIDs)
                            // Create conversation into firestore database
                            conversationRef.add(conversationData).addOnSuccessListener { documentRef ->
                                // Conversation document created, navigate to chat screen with conversation ID
                                val conversationID = documentRef.id

                                // Create conversation in realtime database
                                val database = FirebaseDatabase.getInstance()
                                val chatRef = database.getReference("chats")
                                chatRef.child(conversationID).setValue(hashMapOf("messages" to null))
                                    .addOnSuccessListener {
                                        val bundle = Bundle()
                                        bundle.putString("conversationID", conversationID)
                                        findNavController().navigate(R.id.nav_chat, bundle)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Failed to create conversation", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
            }
        }

        binding.btnAddToCart.setOnClickListener {
            if (userID != null) {
                userViewModel.addProductToCartList(userID, productId)
                Toast.makeText(
                    requireContext(),
                    "Product already added to cart list",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}