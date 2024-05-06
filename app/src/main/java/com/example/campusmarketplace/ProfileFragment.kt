package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentProfileBinding
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

        binding.messageViewBtn.setOnClickListener() {
            findNavController().navigate(R.id.action_nav_profile_to_nav_messageList)
        }

        binding.aboutUsViewBtn.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_aboutUs)
        }

        binding.logoutBtn.setOnClickListener {
            sharedPreferences.edit().remove("userID").apply()

            findNavController().navigate(R.id.nav_login)
        }
    }
}