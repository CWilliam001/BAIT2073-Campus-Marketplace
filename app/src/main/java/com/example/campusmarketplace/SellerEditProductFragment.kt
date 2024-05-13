package com.example.campusmarketplace

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.campusmarketplace.databinding.FragmentSellerEditProductBinding
import com.example.campusmarketplace.model.SellerProduct
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SellerEditProductFragment : Fragment() {
    private lateinit var binding: FragmentSellerEditProductBinding
    private val viewModel: SellerProductViewModel by lazy {
        ViewModelProvider(this).get(SellerProductViewModel::class.java)
    }

    private lateinit var productId: String
    private lateinit var productName: String
    private lateinit var productDescription: String
    private lateinit var productCategory: String
    private lateinit var productPrice: String
    private lateinit var productCondition: String
    private lateinit var productUsageDuration: String
    private lateinit var uploadTime: String
    private lateinit var sellerID: String


    // Define your Spinners
    private lateinit var categorySpinner: Spinner
    private lateinit var conditionSpinner: Spinner
    private lateinit var usageDurationSpinner: Spinner

    private  var productImageUri: Uri = Uri.EMPTY // Initialize with an empty Uri
    private lateinit var getPhotoPicker: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSellerEditProductBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categorySpinner = binding.spCategory
        conditionSpinner = binding.spProductCondition
        usageDurationSpinner = binding.spUsageDuration

        // Setup adapters and populate Spinners
        setupCategorySpinner()
        setupConditionSpinner()
        setupUsageDurationSpinner()

        arguments?.let { bundle ->
            productId = bundle.getString("productID", "")
            productName = bundle.getString("productName", "")
            productDescription = bundle.getString("productDescription", "")
            productCategory = bundle.getString("productCategory", "")
            productPrice = bundle.getString("productPrice", "")
            productCondition = bundle.getString("productCondition", "")
            productUsageDuration = bundle.getString("productUsageDuration", "")
            uploadTime = bundle.getString("uploadTime", "")
            sellerID = bundle.getString("sellerID"," ")

            // Retrieve the image URI
            val imageUrl = bundle.getString("productImage", "")
            productImageUri = Uri.parse(imageUrl)
        }
        // Set the retrieved values to the EditText views
        binding.etProductName.setText(productName)
        binding.etProductDescription.setText(productDescription)
        binding.etProductPrice.setText(productPrice)
        // Set Spinner values
        setSpinnerSelection(categorySpinner, productCategory)
        setSpinnerSelection(conditionSpinner, productCondition)
        setSpinnerSelection(usageDurationSpinner, productUsageDuration)

        // Load and display the image using Picasso or Glide
        Picasso.get().load(productImageUri).into(binding.ivProductImageUpload)

        binding.btnEdit.setOnClickListener {
            if(validateInput()){
                showConfirmationDialog()
            }
        }
        // Initialize the ActivityResultLauncher
        getPhotoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the selected image URI
            uri?.let {
                productImageUri = uri
                // Load the selected image into an ImageView using Picasso or another image loading library
                Picasso.get().load(uri).into(binding.ivProductImageUpload)
            }
        }

        // Add click listener to edit image button
        binding.ivProductImageUpload.setOnClickListener {
            // Launch the image picker
            getPhotoPicker.launch("image/*")
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

    private fun editProduct() {
        uploadTime = getCurrentTimestamp()

        if(validateInput()){
            val productId = arguments?.getString("productID") ?: ""
            val product = SellerProduct(
                productId,
                binding.etProductName.text.toString(),
                binding.etProductDescription.text.toString(),
                binding.spCategory.selectedItem.toString(),
                binding.etProductPrice.text.toString(),
                binding.spProductCondition.selectedItem.toString(),
                binding.spUsageDuration.selectedItem.toString(),
                uploadTime,
                sellerID
            )

            viewModel.updateItem(product, productImageUri)

            Toast.makeText(requireContext(), "Successfully updated product", Toast.LENGTH_SHORT * 3).show()

            // Navigate Back
            findNavController().popBackStack()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())

    }
    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(value)
        spinner.setSelection(position)
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.productCategory)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupConditionSpinner() {
        val conditions = resources.getStringArray(R.array.productCondition)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, conditions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        conditionSpinner.adapter = adapter
    }

    private fun setupUsageDurationSpinner() {
        val durations = resources.getStringArray(R.array.usageDuration)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, durations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        usageDurationSpinner.adapter = adapter
    }

    private fun validateInput(): Boolean {
        val productName = binding.etProductName.text.toString().trim()
        val productDescription = binding.etProductDescription.text.toString().trim()
        val productPrice = binding.etProductPrice.text.toString().trim().toDoubleOrNull()

        // Check if any field is empty
        if (productImageUri == null) {
            Toast.makeText(requireContext(), "Please upload image of product", Toast.LENGTH_SHORT).show()
            return false
        }
        if(productName.isEmpty()){
            binding.etProductName.error = getString(R.string.required_input)
            return false
        }
        if(productDescription.isEmpty()){
            binding.etProductDescription.error = getString(R.string.required_input)
            return false
        }
        if (productPrice == null) {
            binding.etProductPrice.error = getString(R.string.required_input)
            return false
        }
        if (productPrice == 0.0) {
            binding.etProductPrice.error = "Price must be more than 0"
            return false
        }
        return true
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
            .setMessage("Confirm edit product?")
            .setPositiveButton("Save") { _, _ ->
                editProduct()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}