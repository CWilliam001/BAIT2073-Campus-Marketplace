package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.campusmarketplace.databinding.FragmentCardPaymentBinding

class CardPaymentFragment : Fragment() {
    private lateinit var binding: FragmentCardPaymentBinding

    private var userID: String? = null
    private var checkedItemIDs: ArrayList<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCardPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Receive the bundle from the previous fragment
        arguments?.let { bundle ->
            userID = bundle.getString("userID")
            checkedItemIDs = bundle.getStringArrayList("checkedItemIDs")
        }

        // Set onClickListener for the Done button
        binding.btnDone.setOnClickListener {
            if(validateInput()){

            }
        }
    }

    private fun back() {
        binding.etHolderName.setText("")
        binding.etCardNumber.setText("")
        binding.etCvv.setText("")
        binding.spinnerMonth.setSelection(0)
        binding.spinnerYear.setSelection(0)
    }

    private fun validateInput(): Boolean {
        if (binding.etHolderName.text.isEmpty()) {
            binding.etHolderName.setError(getString(R.string.required_input))
            return false
        }

        if (binding.etCardNumber.text.isEmpty()) {
            binding.etCardNumber.setError(getString(R.string.required_input))
            return false
        }

        if (binding.etCvv.text.isEmpty()) {
            binding.etCvv.setError(getString(R.string.required_input))
            return false
        }

        if(binding.etCardNumber.text.length != 16){
            binding.etCardNumber.setError(getString(R.string.card_number_must_be_16_digit))
            return false
        }

        if(binding.etCvv.text.length != 3){
            binding.etCvv.setError(getString(R.string.cvv_must_be_3_digit))
            return false
        }
        return true
    }
}
