package com.example.campusmarketplace

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.campusmarketplace.model.Chat

class ChatViewModel: ViewModel() {
    private val repository = ChatRepository()

    private val _chatLiveData = MutableLiveData<List<Chat>>()
    val chatLiveData: LiveData<List<Chat>> = _chatLiveData

    fun retrieveAllItems(context: Context, conversationID: String) {
        repository.retrieveAllChatItem(context, conversationID) { chatList ->
            _chatLiveData.postValue(chatList)
        }
    }
}