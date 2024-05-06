package com.example.campusmarketplace

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentSignUpBinding
import com.example.campusmarketplace.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.Date
import kotlin.math.sign

class SignUp : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var getPhotoPicker: ActivityResultLauncher<String>
    private lateinit var firestore: FirebaseFirestore
    private var imageStorageURL: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        getPhotoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? -> uri?.let {
                imageStorageURL = uri
                Picasso.get().load(uri).into(binding.uploadProfileImageView)
            }
        }

        binding.uploadProfileImageView.setOnClickListener{
            getPhotoPicker.launch("image/*")
        }

        binding.signUpBtn.setOnClickListener{
            val imageUri = imageStorageURL
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()

            if (imageUri == null) {
                binding.uploadProfileImageTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && phoneNumber.isNotEmpty() && (password == confirmPassword)) {
//                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val signInMethods = task.result?.signInMethods?: emptyList()
//                        if (signInMethods.isNotEmpty()) {
//                            binding.emailEditText.error =
//                                getString(R.string.email_already_registered_error)
//                        }
//                    } else {
//                        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { signUpTask ->
//                            if (signUpTask.isSuccessful) {
//                                val storageRef = FirebaseStorage.getInstance().reference
//                                val userID = signUpTask.result?.user?.uid
//
//                                val imageName = "profile_${userID}"
//                                val imageRef = storageRef.child("user/$imageName")
//
//                                imageStorageURL?.let { uri ->
//                                    imageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
//                                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                                            firestore = FirebaseFirestore.getInstance()
//
//                                            val profileImageUrl = downloadUri.toString()
//                                            val user = User(name, Date(), phoneNumber, profileImageUrl)
//                                            firestore.collection("users").document(userID.toString()).set(user)
//
//                                            val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
//                                            val editor = sharedPreferences.edit()
//                                            editor.putString("userID", userID)
//                                            editor.apply()
//
//                                            findNavController().navigateUp()
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { signUpTask ->
                    if (signUpTask.isSuccessful) {
                        val storageRef = FirebaseStorage.getInstance().reference
                        val userID = signUpTask.result?.user?.uid

                        val imageName = "profile_${userID}"
                        val imageRef = storageRef.child("user/$imageName")

                        imageStorageURL?.let { uri ->
                            imageRef.putFile(uri).addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                    firestore = FirebaseFirestore.getInstance()

                                    val profileImageUrl = downloadUri.toString()
                                    val user = User(name, Date(), phoneNumber, profileImageUrl)
                                    firestore.collection("users").document(userID.toString()).set(user)

                                    val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("userID", userID)
                                    editor.apply()

                                    findNavController().navigateUp()
                                }
                            }
                        }
                    } else {
                        binding.emailEditText.error = getString(R.string.email_already_registered_error)
                    }
                }
            } else {
                if (name.isEmpty()) {
                    binding.nameEditText.error = getString(R.string.name_required_error)
                }

                if (email.isEmpty()) {
                    binding.emailEditText.error = getString(R.string.email_required_error)
                }

                if (password.isEmpty()) {
                    binding.passwordEditText.error = getString(R.string.password_required_error)
                }

                if (confirmPassword.isEmpty()) {
                    binding.confirmPasswordEditText.error =
                        getString(R.string.confirm_password_required_error)
                }

                if (password != confirmPassword) {
                    binding.confirmPasswordEditText.error =
                        getString(R.string.password_not_matched_error)
                }

                if (phoneNumber.isEmpty()) {
                    binding.phoneNumberEditText.error =
                        getString(R.string.phone_number_required_error)
                }
            }
        }
    }
}