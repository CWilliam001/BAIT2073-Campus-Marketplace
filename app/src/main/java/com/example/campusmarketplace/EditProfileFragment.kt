package com.example.campusmarketplace

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentEditProfileBinding
import com.example.campusmarketplace.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.Date

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var getPhotoPicker: ActivityResultLauncher<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var imageStorageURL: Uri? = null
    private var joinedDate: Date = Date()
    private var profileImageUrl: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)
        firestore =  FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        getPhotoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageStorageURL = uri
                Picasso.get().load(uri).transform(RoundedTransformation()).into(binding.uploadProfileImageView)
            }
        }


        if (userID == null) {
            findNavController().navigate(R.id.nav_login)
        } else {
            val userDocRef = firestore.collection("users").document(userID!!)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name")
                        val email = firebaseAuth.currentUser?.email
                        val phoneNumber = document.getString("phoneNumber")
                        profileImageUrl = document.getString("profileImageUrl")
                        val address = document.getString("address")
                        val states = document.getString("states")
                        val zipCode = document.getString("zipCode")

                        Picasso.get().load(profileImageUrl).transform(RoundedTransformation()).into(binding.uploadProfileImageView)
                        binding.nameEditText.setText(userName)
                        binding.emailEditText.setText(email)
                        binding.phoneNumberEditText.setText(phoneNumber)
                        binding.addressEditText.setText(address)
                        binding.stateSpinner.setSelection(getStateIndex(states))
                        binding.zipCodeEditText.setText(zipCode)
                        joinedDate = document.getDate("registerDate")!!
                    }
                }
        }

        binding.uploadProfileImageView.setOnClickListener {
            getPhotoPicker.launch("image/*")
        }

        binding.saveBtn.setOnClickListener {
            val imageUri = imageStorageURL
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            val address = binding.addressEditText.text.toString().trim()
            val states = binding.stateSpinner.selectedItemPosition
            val zipCode = binding.zipCodeEditText.text.toString().trim()

            val userDocRef = firestore.collection("users").document(userID!!)

            if (name.isNotEmpty() && email.isNotEmpty() && phoneNumber.isNotEmpty() && address.isNotEmpty() && zipCode.isNotEmpty() && (imageUri != null || profileImageUrl.toString().isNotEmpty())) {
                if (userID != null) {
//                    val statesArray = resources.getStringArray(R.array.arrStates)
//                    val selectedState = statesArray[states]
//
//                    if (imageStorageURL.toString() != profileImageUrl) {
//                        val imageFileName = "profile_${userID}"
//                        val imageRef = storageReference.child("user/$imageFileName")
//                        val uploadTask = imageRef.putFile(imageUri)
//
//                        uploadTask.continueWithTask { task ->
//                            if (!task.isSuccessful) {
//                                task.exception?.let {
//                                    throw it
//                                }
//                            }
//                            imageRef.downloadUrl
//                        }.addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                val downloadUri = task.result
//                                val user = User(name, joinedDate, phoneNumber, downloadUri.toString(), address, selectedState, zipCode)
//                                updateUser(userDocRef, user)
//                            }
//                        }
//                    } else {
//                        val user = User(name, joinedDate, phoneNumber, profileImageUrl.toString(), address, selectedState, zipCode)
//                        updateUser(userDocRef, user)
//                    }
                }
            } else {
                if (imageUri == null) {
                    binding.changePhotoTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.changePhotoTextView.text = getString(R.string.profile_image_required_error)
                }

                if (name.isEmpty()) {
                    binding.nameEditText.error = getString(R.string.name_required_error)
                }

                if (email.isEmpty()) {
                    binding.emailEditText.error = getString(R.string.email_required_error)
                }

                if (phoneNumber.isEmpty()) {
                    binding.phoneNumberEditText.error =
                        getString(R.string.phone_number_required_error)
                }

                if (address.isEmpty()) {
                    binding.addressEditText.error =
                        getString(R.string.address_required_error)
                }

                if (zipCode.isEmpty()) {
                    binding.zipCodeEditText.error =
                        getString(R.string.zip_code_required_error)
                }
            }
        }
    }

    private fun getStateIndex(state: String?): Int {
        val states = resources.getStringArray(R.array.arrStates)
        return states.indexOf(state)
    }

    private fun updateUser(userDocRef: DocumentReference, user: User) {
        userDocRef.set(user)
            .addOnSuccessListener {
                // User data updated successfully
                Toast.makeText(requireContext(),
                    getString(R.string.profile_updated_label), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_profile)
            }
            .addOnFailureListener { exception ->
                // Handle error updating user data in Firestore
            }
    }
}