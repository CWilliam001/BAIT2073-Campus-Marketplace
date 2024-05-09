package com.example.campusmarketplace

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmarketplace.databinding.FragmentChatBinding
import com.example.campusmarketplace.model.Chat
import com.google.firebase.database.FirebaseDatabase

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var chatData: ArrayList<Chat>


    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        binding = FragmentChatBinding.inflate(inflater, container, false)
        adapter = ChatAdapter(userID.toString())
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val args = arguments

        val conversationID = args?.getString("conversationID")
        Log.d("ChatFragment", "Chat Fragment Conversation ID: $conversationID")

        viewModel.retrieveAllItems(requireContext(), conversationID.toString())
        viewModel.chatLiveData.observe(viewLifecycleOwner) { chatList ->
            adapter.setChat(chatList)
            adapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (message.isNotEmpty()) {
                val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                val userID = sharedPreferences.getString("userID", null)
                val conversationID = arguments?.getString("conversationID")

                if (userID != null && conversationID != null) {
                    // Get a reference to the database
                    val database = FirebaseDatabase.getInstance()
                    val chatRef = database.getReference("chats").child(conversationID).child("messages")
                    val messageId = chatRef.push().key

                    val timestamp = System.currentTimeMillis()
                    val chat = Chat(userID, message, "text", timestamp)

                    if (messageId != null) {
                        chatRef.child(messageId).setValue(chat)
                            .addOnSuccessListener {
                                Log.d("ChatFragment", "Message sent successfully")
                                binding.messageEditText.text?.clear()
                                binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("ChatFragment", "Failed to send message", exception)
                            }
                    }
                }
            }
        }
    }
}