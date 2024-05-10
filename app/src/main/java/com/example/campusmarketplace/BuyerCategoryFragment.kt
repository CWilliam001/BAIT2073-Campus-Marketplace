package com.example.campusmarketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentBuyerCategoryBinding

class BuyerCategoryFragment : Fragment() {
    private lateinit var binding: FragmentBuyerCategoryBinding
    private lateinit var buyerCategoryAdapter: BuyerCategoryAdapter
    private lateinit var recyclerView: RecyclerView

    private val buyerCategoryViewModel: BuyerCategoryViewModel by lazy {
        ViewModelProvider(this).get(BuyerCategoryViewModel::class.java)
    }

override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    // Inflate the layout for this fragment
    binding = FragmentBuyerCategoryBinding.inflate(layoutInflater,container,false)
    return binding.root
}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.BuyerCategoryRecyclerview // Initialize recyclerView from the binding

        buyerCategoryAdapter = BuyerCategoryAdapter(requireContext())
        recyclerView.adapter = buyerCategoryAdapter

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        buyerCategoryViewModel.retrieveAllItems()
        buyerCategoryViewModel.productLiveData.observe(viewLifecycleOwner) { productList ->
            buyerCategoryAdapter.setProductCategories(productList)
        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }

    }

}