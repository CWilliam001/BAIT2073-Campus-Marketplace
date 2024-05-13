package com.example.campusmarketplace

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.campusmarketplace.model.Chat
import com.google.android.play.integrity.internal.s
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.Tag
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore

class ChatRepository {

    private val database = Firebase.database
    private val chatRef = database.getReference("chats")
    fun retrieveAllChatItem(conversationID: String, liveData: MutableLiveData<List<Chat>>) {
        // Get reference to the specific conversation node in the database
        val conversationNodeRef = chatRef.child(conversationID).child("messages")

        // Query messages to the specific conversation node in the database
        val query = conversationNodeRef.orderByChild("timestamp").limitToLast(30)

        // Add a listener to fetch messages from the database
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatItems: List<Chat> = snapshot.children.map { dataSnapshot ->
                    dataSnapshot.getValue(Chat::class.java)!!
                }

                liveData.postValue(chatItems)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors
            }
        })
    }

    fun insertChatItem(conversationID: String, chatItem: Chat) {

//        Log.d(ContentValues.TAG, "Insert Item in Repository")
//        Log.d(ContentValues.TAG, "$chatItem")
//        Log.d(ContentValues.TAG, "Conversation ID: $conversationID")
        val conversationNodeRef = chatRef.child(conversationID).child("messages")

        val chatKey = conversationNodeRef.push().key ?:return // Exit if key is null

        conversationNodeRef.child(chatKey).setValue(chatItem)
            .addOnSuccessListener {
                Log.d(TAG, "Chat item inserted successfully")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to send message, $exception")
            }
    }
}