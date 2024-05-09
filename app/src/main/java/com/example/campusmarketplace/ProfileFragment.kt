package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentProfileBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
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
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.nameTextView.text = userName
                    Picasso.get().load(profileImageUrl).transform(RoundedTransformation()).into(binding.profileImageView)
                }
            }
        }

        binding.conversationViewBtn.setOnClickListener() {
            findNavController().navigate(R.id.action_nav_profile_to_nav_conversationList)
        }

        binding.editProfileViewBtn.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_editProfile)
        }

        binding.changePasswordViewBtn.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_changePassword)
        }

        binding.aboutUsViewBtn.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_aboutUs)
        }

        binding.logoutBtn.setOnClickListener {
            sharedPreferences.edit().remove("userID").apply()

            findNavController().navigate(R.id.nav_login)
        }


//        binding.contactSellerBtn.setOnClickListener {
//            // Chia Choong's User UID in Firebase Authentication = "LZ9l4C1b1OaeUvIKDuw6i9F0Bn42"
//            // Use your own User UID to find you and Chia Choong's User UID combination array exist in FirebaseFirestore or not
//            // If exist then redirect them to nav_chat page with
//            // var bundle = Bundle()
//            // bundle.putSerializable("conversationID", document.id)
//            // The document.id is come from conversations collection in FirebaseFirestore
//            // If the combination array does not exist then
//            // Create a conversation document with random ID then set userIDs = [your UID, Chia Choong's UID]
//            // After that return the document ID that has been generated from FirebaseFirestore
//            // Create an object in Firebase Realtime database with the documentID under chats
//            // {
//            //  "jkashfdsikfhasuif":
//            //                      messages: null
//            // }
//
//            val sellerUID = "LZ9l4C1b1OaeUvIKDuw6i9F0Bn42"
//            if (userID != null) {
//                val firestore = FirebaseFirestore.getInstance()
//                val conversationRef = firestore.collection("conversations")
//
//                val query = conversationRef.whereArrayContains("userIDs", userID).whereArrayContains("userIDs", sellerUID)
//
//                query.get().addOnSuccessListener { documents ->
//                    if (!documents.isEmpty) {
//                        val conversationID = documents.first().id
//                        val bundle = Bundle()
//                        bundle.putString("conversationID", conversationID)
//                        findNavController().navigate(R.id.nav_chat, bundle)
//                    } else {
//                        val userIDs = listOf(userID, sellerUID)
//                        val conversationData = hashMapOf("userIDs" to userIDs)
//
//                        conversationRef.add(conversationData).addOnSuccessListener { documentRef ->
//                            // Conversation document created, navigate to chat screen with conversation ID
//                            val conversationID = documentRef.id
//
//                            val database = FirebaseDatabase.getInstance()
//                            val chatRef = database.getReference("chats")
//                            chatRef.child(conversationID).setValue(hashMapOf("messages" to null))
//                                .addOnSuccessListener {
//                                    val bundle = Bundle()
//                                    bundle.putString("conversationID", conversationID)
//                                    findNavController().navigate(R.id.nav_chat, bundle)
//                                }
//                                .addOnFailureListener { e ->
//                                    Toast.makeText(requireContext(), "Failed to create conversation", Toast.LENGTH_SHORT).show()
//                                }
//                        }
//                    }
//                }
//            }
//        }
    }
}