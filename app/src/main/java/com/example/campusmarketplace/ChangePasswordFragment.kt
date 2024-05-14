package com.example.campusmarketplace

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {
            val oldPassword = binding.oldPasswordEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            val currentUser = FirebaseAuth.getInstance().currentUser

            // Regex pattern to validate password with at least 6 characters, 1 alphabet, and 1 digit
            val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}\$")

            // Check if the old password is not empty
            if (oldPassword.isNotEmpty()) {

                // Re-authenticate the user with the provided old password
                val credential = EmailAuthProvider.getCredential(currentUser?.email!!, oldPassword)
                currentUser.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        // Check if the re-authentication was successful
                        if (reauthTask.isSuccessful) {
                            // Check if new password and confirm password are not empty
                            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                // Validate the new password
                                if (newPassword.matches(passwordPattern)) {
                                    if (newPassword == confirmPassword) {
                                        currentUser.updatePassword(newPassword)
                                            .addOnCompleteListener { updatePasswordTask ->
                                                if (updatePasswordTask.isSuccessful) {
                                                    Toast.makeText(requireContext(),
                                                        getString(R.string.password_updated_successfully_label), Toast.LENGTH_SHORT).show()
                                                    findNavController().navigate(R.id.nav_profile)
                                                } else {
                                                    Toast.makeText(requireContext(),
                                                        getString(R.string.failed_to_update_password_label), Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        binding.confirmPasswordEditText.error = getString(R.string.password_not_matched_error)
                                    }
                                } else {
                                    binding.newPasswordEditText.error = getString(R.string.invalid_password_format_error)
                                }
                            } else {
                                if (newPassword.isEmpty()) {
                                    binding.newPasswordEditText.error = getString(R.string.new_password_required_error)
                                }

                                if (confirmPassword.isEmpty()) {
                                    binding.confirmPasswordEditText.error = getString(R.string.confirm_password_required_error)
                                }
                            }
                        } else {
                            binding.oldPasswordEditText.error = getString(R.string.incorrect_old_password_label)
                        }
                    }
            } else {
                binding.oldPasswordEditText.error = getString(R.string.old_password_required_error)
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