package com.example.campusmarketplace

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.campusmarketplace.model.Conversation

class ConversationViewModel: ViewModel() {
    private val repository = ConversationRepository()

    private val _conversationLiveData = MutableLiveData<List<Conversation>>()
    val conversationLiveData: LiveData<List<Conversation>> = _conversationLiveData

    fun retrieveAllItems(context: Context) {
        repository.retrieveAllConversationItem(context,_conversationLiveData)
    }
}