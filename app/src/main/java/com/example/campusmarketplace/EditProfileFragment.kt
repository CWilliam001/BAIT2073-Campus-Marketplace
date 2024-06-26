package com.example.campusmarketplace

import android.app.AlertDialog
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

        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        getPhotoPicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    imageStorageURL = uri
                    Picasso.get().load(uri).transform(RoundedTransformation())
                        .into(binding.uploadProfileImageView)
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
                        val phoneNumber = document.getString("phoneNumber")
                        profileImageUrl = document.getString("profileImageUrl")
                        val address = document.getString("address")
                        val states = document.getString("states")
                        val zipCode = document.getString("zipCode")

                        Picasso.get().load(profileImageUrl).transform(RoundedTransformation())
                            .into(binding.uploadProfileImageView)
                        binding.nameEditText.setText(userName)
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
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            val address = binding.addressEditText.text.toString().trim()
            val states = binding.stateSpinner.selectedItemPosition
            val zipCode = binding.zipCodeEditText.text.toString().trim()

            val userDocRef = firestore.collection("users").document(userID!!)

            if (name.isNotEmpty() && phoneNumber.isNotEmpty() && address.isNotEmpty() && zipCode.isNotEmpty() &&
                name.contains(Regex("[a-zA-Z0-9]")) && zipCode.length == 5 &&
                (imageUri != null || profileImageUrl.toString().isNotEmpty())
            ) {
                if (userID != null) {
                    // How to perform update profile in firestore
                    // Need to consider bout profileImageUrl also if they had changed the profileImageUrl then need to save the new value of profileImageUrl as well


                    val statesArray = resources.getStringArray(R.array.arrStates)
                    val selectedState = statesArray[states]
                    var updateData = hashMapOf<String, Any>(
                        "name" to name,
                        "phoneNumber" to phoneNumber,
                        "address" to address,
                        "states" to selectedState,
                        "zipCode" to zipCode,
                    )

                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {

                        if (imageUri != null) {
                            // Upload the new profile image to Firebase Storage
                            val imageName = "profile_${userID}"
                            val imageRef = storageReference.child("user/$imageName")
                            imageRef.putFile(imageUri)
                                .addOnSuccessListener { taskSnapshot ->
                                    imageRef.downloadUrl.addOnSuccessListener { uri ->

                                        // Insert profileImageUrl into hashmap
                                        val imageUrl = uri.toString()
                                        updateData["profileImageUrl"] = imageUrl

                                        // Update the user document in Firestore
                                        userDocRef.update(updateData)
                                            .addOnCompleteListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.profile_updated_label),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                findNavController().navigate(R.id.nav_profile)
                                            }
                                            .addOnFailureListener {
                                                // Handle error updating user data in firestore
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.update_data_error),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }

                                }
                                .addOnFailureListener {
                                    // Handle error uploading profile image to Firebase Storage
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.upload_image_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            // Update without profile image changes
                            userDocRef.update(updateData)
                                .addOnCompleteListener {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.profile_updated_label),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(R.id.nav_profile)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.update_data_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        // Clear session let them re-login again
                        sharedPreferences.edit().remove("userID").apply()
                        findNavController().navigate(R.id.nav_login)
                    }
                }
            } else {
                if (imageUri == null) {
                    binding.changePhotoTextView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                    binding.changePhotoTextView.text =
                        getString(R.string.profile_image_required_error)
                }

                if (name.isEmpty()) {
                    binding.nameEditText.error = getString(R.string.name_required_error)
                }

                if (name.contains(Regex("[^a-zA-Z0-9 ]"))) {
                    binding.nameEditText.error = getString(R.string.accept_alpha_numeric_error)
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

                if (zipCode.length != 5) {
                    binding.zipCodeEditText.error =
                        getString(R.string.invalid_zip_code_error)
                }
            }
        }

        binding.btnUp.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmation")
            builder.setMessage("Discard this changes?")
            builder.setPositiveButton("Discard") { _, _ ->
                // Perform up navigation
                findNavController().navigateUp()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
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
                Toast.makeText(
                    requireContext(),
                    getString(R.string.profile_updated_label), Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.nav_profile)
            }
            .addOnFailureListener { exception ->
                // Handle error updating user data in Firestore
            }
    }
}