package com.example.campusmarketplace

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentCardPaymentBinding
import com.example.campusmarketplace.model.SellerProduct
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardPaymentFragment : Fragment() {
    private lateinit var binding: FragmentCardPaymentBinding

    private lateinit var sharedPreferences: SharedPreferences
    private var userID: String? = null

    private lateinit var productList: ArrayList<SellerProduct>

    private val viewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    private val productViewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCardPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize sharedPreferences and userID in onViewCreated
        sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        userID = sharedPreferences.getString("userID", null)


        // Receive the bundle from the previous fragment
        arguments?.let { bundle ->
            userID = bundle.getString("userID")
            productList = bundle.getParcelableArrayList("checkedItems") ?: arrayListOf()
        }

        // Set onClickListener for the Done button
        binding.btnDone.setOnClickListener {
            if (validateInput()) {
                showConfirmationDialog()
            }
        }

        binding.btnUp.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmation")
            builder.setMessage("Cancel Payment?")
            builder.setPositiveButton("Yes") { _, _ ->
                // Perform up navigation
                findNavController().navigateUp()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun validateInput(): Boolean {
        if (binding.etHolderName.text.isEmpty()) {
            binding.etHolderName.error = getString(R.string.required_input)
            return false
        }

        if (binding.etCardNumber.text.isEmpty()) {
            binding.etCardNumber.error = getString(R.string.required_input)
            return false
        }

        if (binding.etCardNumber.text.length != 16) {
            binding.etCardNumber.error = getString(R.string.card_number_must_be_16_digit)
            return false
        }

        if (binding.etCvv.text.isEmpty()) {
            binding.etCvv.error = getString(R.string.required_input)
            return false
        }

        if (binding.etCvv.text.length != 3) {
            binding.etCvv.error = getString(R.string.cvv_must_be_3_digit)
            return false
        }

        if (!validateExpiryDate()) {
            return false
        }

        return true
    }

    private fun validateExpiryDate(): Boolean {
        val expiryDate = binding.etExpiredDate.text.toString()
        val parts = expiryDate.split("/")

        if (parts.size == 2) {
            val monthString = parts[0]
            val yearString = parts[1]

            // Check if month is two digits and year is two digits
            if (monthString.length == 2 && yearString.length == 2) {
                val month = monthString.toIntOrNull()
                val year = yearString.toIntOrNull()

                if (month != null) {
                    if (month in 1..12) {
                        if (year != null) {
                            if (year >= 0) { // Assuming year is in YY format

                                // Get the current year and month
                                val currentYear = Calendar.getInstance()
                                    .get(Calendar.YEAR) % 100 // Get last two digits of the year
                                val currentMonth = Calendar.getInstance()
                                    .get(Calendar.MONTH) + 1 // Month is zero-based, so add 1

                                // Check if the expiry date is not in the past
                                if (year > currentYear || (year == currentYear && month > currentMonth)) {
                                    // Expiry date is valid
                                    binding.etExpiredDate.error = null
                                    return true
                                } else {
                                    // Expiry date is in the past
                                    binding.etExpiredDate.error = "Expired card"
                                    return false
                                }
                            } else {
                                // Year is invalid
                                binding.etExpiredDate.error = "Invalid year"
                                return false
                            }
                        } else {
                            // Year input is not a number
                            binding.etExpiredDate.error = "Year must be a number"
                            return false
                        }
                    } else {
                        // Month is not in the range 1-12
                        binding.etExpiredDate.error = "Invalid month"
                        return false
                    }
                } else {
                    // Month input is not a number
                    binding.etExpiredDate.error = "Month must be a number (MM/YY)"
                    return false
                }
            } else {
                binding.etExpiredDate.error = "Invalid input format (MM/YY)"
                return false
            }
        } else {
            binding.etExpiredDate.error = "Invalid input format (MM/YY)"
            return false
        }
    }


    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
            .setMessage("Confirm payment?")
            .setPositiveButton("Yes") { dialog, which ->
                for (product in productList) {
                    if (userID != null) {
                        product.paymentMethod = "Card Payment"
                        product.paymentDate = getCurrentTimestamp()
                        product.buyerID = userID.toString()
                        viewModel.deleteCartItem(userID!!, product)
                        productViewModel.updateOrderItem(product)
                    }
                }

                Toast.makeText(requireContext(), "Successfully purchased", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(R.id.action_nav_cardPayment_to_nav_buyerToPickUp)
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
