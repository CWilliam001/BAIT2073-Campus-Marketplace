package com.example.campusmarketplace

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.campusmarketplace.model.Chat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ChatRepository {

    private val database = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val chatRef = database.getReference("chats")
    fun retrieveAllChatItem(context: Context, conversationID: String, callback: (List<Chat>) -> Unit) {
        val chatList = mutableListOf<Chat>()

        // Get reference to the specific conversation node in the database
        val conversationNodeRef = chatRef.child(conversationID).child("messages")

        // Query messages to the specific conversation node in the database
        val query = conversationNodeRef.orderByChild("timestamp").limitToLast(30)

        // Add a listener to fetch messages from the database
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot in dataSnapshot.children) {
                    // Parse each message from dataSnapshot and add it to the list
                    var chat = messageSnapshot.getValue(Chat::class.java)
                    if (chat != null) {
                        chatList.add(Chat(chat.senderId, chat.content, chat.datatype, chat.timestamp))
                    }
                }
                // Once all messages are fetched, invoke the callback function with the list of messages
                callback(chatList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors
            }
        })

    }
}