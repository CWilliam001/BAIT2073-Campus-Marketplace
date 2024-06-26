package com.example.campusmarketplace

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentChatItemBinding
import com.example.campusmarketplace.model.Chat
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter internal constructor(
    private val context: Context,
    private val userID: String
) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chat = emptyList<Chat>()

    inner class ChatViewHolder(private val binding: FragmentChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, current: Chat, userID: String) {

            binding.messageTextView.text = current.content

            if (current.senderId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("users").document(current.senderId)
                userRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        binding.nameTextView.text = documentSnapshot.getString("name")
                        val profileImageUrl = documentSnapshot.getString("profileImageUrl")
                        Picasso.get().load(profileImageUrl).transform(RoundedTransformation())
                            .into(binding.profileImageView)
                    }
                }
            }
            binding.timeTextView.text = convertTimestampToDateTime(current.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding =
            FragmentChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val current = chat[position]
        holder.bind(context, current, userID)
    }

    override fun getItemCount() = chat.size

    internal fun setChat(chat: List<Chat>) {
        this.chat = chat
        notifyDataSetChanged()
    }

    fun convertTimestampToDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }

}