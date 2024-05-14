package com.example.campusmarketplace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusmarketplace.model.Chat
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _chatLiveData = MutableLiveData<List<Chat>>()
    val chatLiveData: LiveData<List<Chat>> = _chatLiveData

    fun retrieveAllItems(conversationID: String) {
        repository.retrieveAllChatItem(conversationID, _chatLiveData)
    }

    fun insertItem(conversationID: String, chat: Chat) {
        viewModelScope.launch {
//            Log.d(TAG, "Passing data from view model to repository")
            repository.insertChatItem(conversationID, chat)
        }
    }
}