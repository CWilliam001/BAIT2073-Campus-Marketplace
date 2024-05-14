package com.example.campusmarketplace

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmarketplace.databinding.FragmentChatBinding
import com.example.campusmarketplace.model.Chat

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private var conversationID: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)
        adapter = ChatAdapter(requireContext(), userID.toString())
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val args = arguments
        val conversationID = args?.getString("conversationID")
        this.conversationID = conversationID.toString()
        Log.d("ChatFragment", "Chat Fragment Conversation ID: $conversationID")

        viewModel.retrieveAllItems(conversationID.toString())
        viewModel.chatLiveData.observe(viewLifecycleOwner) { chatList ->
            adapter.setChat(chatList)
//            adapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }


        binding.sendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            Log.d(TAG, "Message: $message")
            if (message.isNotEmpty()) {
                if (userID != null && conversationID != null) {
                    val chat = Chat(userID.toString(), message, "text", System.currentTimeMillis())
                    viewModel.insertItem(conversationID.toString(), chat)
                    binding.messageEditText.text?.clear()
                    binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }

        }

        binding.btnUp.setOnClickListener {
            // Perform up navigation
            findNavController().navigateUp()
        }
    }
}