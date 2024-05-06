package com.example.campusmarketplace

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        binding.logInBtn.setOnClickListener {
//            Log.d("LoginFragment", "Login button clicked")
//            Toast.makeText(requireContext(), "Forgot Password Clicked", Toast.LENGTH_SHORT).show()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                if(email.isEmpty()) {
                    binding.emailEditText.error = (getString(R.string.email_required_error))
                }

                if(password.isEmpty()) {
                    binding.passwordEditText.error = (getString(R.string.password_required_error))
                }
            }
        }

        binding.forgotPasswordTextView.setOnClickListener{
            Toast.makeText(requireContext(), "Forgot Password Clicked", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_loginFragment_to_forgotPassword)
        }

        binding.signUpTextView.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_signUp)
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) {
                task ->
                if(task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userId = user?.uid

                    userId?.let {
                        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("userID", userId)
                        editor.apply()

                    }
                    findNavController().navigateUp()
                } else {
                    binding.signInMessage.text = context?.getString(R.string.email_password_error)
                    binding.signInMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
    }
}