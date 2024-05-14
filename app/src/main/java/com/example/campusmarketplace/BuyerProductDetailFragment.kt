package com.example.campusmarketplace

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentBuyerProductDetailBinding
import com.example.campusmarketplace.model.SellerProduct
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class BuyerProductDetailFragment : Fragment() {
    private lateinit var binding: FragmentBuyerProductDetailBinding
    private lateinit var product: SellerProduct
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
            product = bundle.getParcelable("buyerProduct")!!
            product?.let { product ->
                productImageUri = Uri.parse(product.productImage)
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

            // set the retrieved values to textView
            // Load and display the image using Picasso or Glide
            Picasso.get().load(productImageUri).into(binding.imgProductImage)
            binding.tvProductName.text = product.productName
            binding.tvProductPrice.text = String.format("RM %.2f", product.productPrice.toDouble())
            binding.tvCondition.text = product.productCondition
            binding.tvUsage.text = product.productUsageDuration
            binding.tvCategory.text = product.productCategory
            binding.tvDescription.text = product.productDescription
            binding.tvUploadTime.text = product.uploadTime

            binding.btnUp.setOnClickListener {
                // Perform up navigation
                findNavController().navigateUp()
            }

            binding.btnLike.setOnClickListener {
                if (userID != null) {
                    userViewModel.addProductToLikeList(userID, product.productID)
                    Toast.makeText(
                        requireContext(),
                        "Successfully added to likes",
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
                                            if (otherUserID == product.sellerID) {
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
                                findNavController().navigate(
                                    R.id.action_nav_buyerProductDetail_to_nav_chat,
                                    bundle
                                )
                            } else {
//                            Toast.makeText(requireContext(), "Chatting with New Seller: $sellerID", Toast.LENGTH_SHORT).show()
                                val userIDs = listOf(userID, product.sellerID)
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
                                                bundle.putSerializable(
                                                    "conversationID",
                                                    conversationID
                                                )
                                                findNavController().navigate(
                                                    R.id.action_nav_buyerProductDetail_to_nav_chat,
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

            binding.btnAddToCart.setOnClickListener {
                if (product.paymentMethod.trim().isEmpty()) {
                    if (userID != null) {
                        userViewModel.addProductToCartList(userID, product.productID)
                        Toast.makeText(
                            requireContext(),
                            "Successfully added to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    Toast.makeText(
                        requireContext(),
                        "Product already sold",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}