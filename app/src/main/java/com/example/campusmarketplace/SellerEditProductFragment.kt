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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SellerEditProductFragment : Fragment() {
    private lateinit var binding: FragmentSellerEditProductBinding
    private lateinit var storageReference: StorageReference
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

    private var imageStorageURL: Uri? = null
    private var profileImageUrl: String? = ""
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

        storageReference = FirebaseStorage.getInstance().reference

        getPhotoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageStorageURL = uri
                Picasso.get().load(uri).into(binding.ivProductImageUpload)
            }
        }

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
            profileImageUrl= bundle.getString("productImage", "")
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
        Picasso.get().load(profileImageUrl).into(binding.ivProductImageUpload)

        // Add click listener to edit image button
        binding.ivProductImageUpload.setOnClickListener {
            // Launch the image picker
            getPhotoPicker.launch("image/*")
        }

        binding.btnEdit.setOnClickListener {
            if(validateInput()){
                showConfirmationDialog()
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

    private fun editProduct() {
        uploadTime = getCurrentTimestamp() // Ensure getCurrentTimestamp() provides the correct format
        val productId = arguments?.getString("productID") ?: ""
        val productName = binding.etProductName.text.toString()
        val productDescription = binding.etProductDescription.text.toString()
        val productCategory = binding.spCategory.selectedItem.toString()
        val productPrice = binding.etProductPrice.text.toString()
        val productCondition = binding.spProductCondition.selectedItem.toString()
        val productUsageDuration = binding.spUsageDuration.selectedItem.toString()
        val imageUri = imageStorageURL

        if (validateInput()) {
            val updateData = hashMapOf<String, Any>(
                "productName" to productName,
                "productDescription" to productDescription,
                "productCategory" to productCategory,
                "productPrice" to productPrice,
                "productCondition" to productCondition,
                "productUsageDuration" to productUsageDuration,
                "uploadTime" to uploadTime
            )

            if (imageUri != null) {
                // Upload the new profile image to Firebase Storage
                val imageName = "${productId}.jpg"
                val imageRef = storageReference.child("images/$imageName")
                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Insert imageUrl into updateData
                            val imageUrl = uri.toString()
                            updateData["productImage"] = imageUrl

                            // Call the ViewModel to update the product
                            val viewModel: SellerProductViewModel by lazy {
                                ViewModelProvider(this).get(SellerProductViewModel::class.java)
                            }
                            viewModel.updateItem(productId, updateData)

                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("EditProduct", "Failed to upload new image to Storage: ${e.message}")
                        // Handle image upload failure (e.g., show error to user)
                    }
            } else {
                // Update without profile image changes
                val viewModel: SellerProductViewModel by lazy {
                    ViewModelProvider(this).get(SellerProductViewModel::class.java)
                }
                viewModel.updateItem(productId, updateData)
            }
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
        if (profileImageUrl == null) {
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