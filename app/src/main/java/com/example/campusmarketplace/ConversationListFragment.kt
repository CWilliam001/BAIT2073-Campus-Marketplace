package com.example.campusmarketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmarketplace.databinding.FragmentConversationListBinding

class ConversationListFragment : Fragment() {

    private lateinit var binding: FragmentConversationListBinding
    private lateinit var adapter: ConversationListAdapter

    private val viewModel: ConversationViewModel by lazy {
        ViewModelProvider(this)[ConversationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationListBinding.inflate(inflater, container, false)
        adapter = ConversationListAdapter(this)
        binding.conversationRecyclerView.adapter = adapter
        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.retrieveAllItems(requireContext())
        viewModel.conversationLiveData.observe(viewLifecycleOwner) { conversationData ->
            adapter.setConversation(conversationData)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }

    fun navigateToChatFragment(conversationID: String) {
        val bundle = Bundle()
        bundle.putSerializable("conversationID", conversationID)
        findNavController().navigate(R.id.action_nav_conversationList_to_nav_chat, bundle)
    }
}