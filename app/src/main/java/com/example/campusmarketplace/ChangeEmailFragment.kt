package com.example.campusmarketplace

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
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

            // Regex pattern to validate email with TARC domain
            val emailPattern = Regex("[a-zA-Z0-9._%+-]+@(?:student\\.)?tarc\\.edu\\.my")

            if (password.isNotEmpty()) {
                if (currentUser != null && currentUser.email != null) {
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)

                    // Re-authenticate the user with the provided credential
                    currentUser.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            // Re-authentication successful
                            if (reauthTask.isSuccessful) {
                                val newEmail = binding.newEmailEditText.text.toString()
                                // Check if the new email is not empty
                                if (newEmail.isNotEmpty()) {
                                    if (newEmail.matches(emailPattern)) {
                                        currentUser.verifyBeforeUpdateEmail(newEmail)
                                            .addOnCompleteListener { updateEmailTask ->
                                                // Email verification sent
                                                if (updateEmailTask.isSuccessful) {
                                                    currentUser.sendEmailVerification()
                                                        .addOnCompleteListener { verificationTask ->
                                                            // Email verification sent
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
                                        binding.newEmailEditText.error = getString(R.string.invalid_email_format_error)
                                    }
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
}