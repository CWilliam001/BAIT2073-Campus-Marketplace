package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.campusmarketplace.databinding.FragmentChangeEmailBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangeEmailFragment : Fragment() {

    private lateinit var binding: FragmentChangeEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        binding.sendBtn.setOnClickListener {
            val password = binding.passwordEditText.text.toString()

            if (password.isNotEmpty()) {
                if (currentUser != null && currentUser.email != null) {
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)

                    // Re-authenticate the user with the provided credential
                    currentUser.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                val newEmail = binding.newEmailEditText.text.toString()
                                if (newEmail.isNotEmpty()) {
                                    currentUser.verifyBeforeUpdateEmail(newEmail)
                                        .addOnCompleteListener { updateEmailTask ->
                                            if (updateEmailTask.isSuccessful) {
                                                currentUser.sendEmailVerification()
                                                    .addOnCompleteListener { verificationTask ->
                                                        if (verificationTask.isSuccessful) {
                                                            Toast.makeText(requireContext(), "Email verification sent", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(requireContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            }
                                        }

                                    // Update the user's email address
                                    currentUser.updateEmail(newEmail)
                                } else {
                                    binding.newEmailEditText.error = getString(R.string.new_email_required_error)
                                }
                            } else {
                                binding.passwordEditText.error = getString(R.string.incorrect_password_error)
                            }
                        }
                }
            } else {
                binding.passwordEditText.error = getString(R.string.password_required_error)
            }

        }
    }
}