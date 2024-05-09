package com.example.campusmarketplace

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarketplace.databinding.FragmentConversationItemViewBinding
import com.example.campusmarketplace.model.Conversation
import com.squareup.picasso.Picasso

class ConversationListAdapter internal constructor(private val fragment: Fragment) :
    RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder>() {
        private var conversation = emptyList<Conversation>()

        inner class ConversationViewHolder(private val binding: FragmentConversationItemViewBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(current:Conversation){
                    binding.itemTitle.text = current.name
                    Picasso.get().load(current.imageUrl).transform(RoundedTransformation()).into(binding.itemImage)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = FragmentConversationItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }

    override fun getItemCount() = conversation.size

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val current = conversation[position]
        holder.bind(current)
        holder.itemView.setOnClickListener {
            val conversationID = current.id
            (fragment as? ConversationListFragment)?.navigateToChatFragment(conversationID)
        }
    }

    internal fun setConversation(conversation: List<Conversation>) {
        this.conversation = conversation
        notifyDataSetChanged()
    }
}