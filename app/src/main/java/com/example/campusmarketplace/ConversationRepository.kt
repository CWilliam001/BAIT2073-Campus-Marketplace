package com.example.campusmarketplace

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import com.example.campusmarketplace.model.Conversation
import com.google.android.play.integrity.internal.i
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale.filter

class ConversationRepository {

//    private val db = Firebase.database
    private lateinit var firestore: FirebaseFirestore
//    private val conversationRef = db.getReference("conversations")

    fun retrieveAllConversationItem(context: Context,liveData: MutableLiveData<List<Conversation>>) {
        val conversationList = mutableListOf<Conversation>()
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        Log.d(TAG, "userID: $userID")

        firestore = FirebaseFirestore.getInstance()

        val conversationRef = firestore.collection("conversations")

        if (userID != null) {
            conversationRef.whereArrayContains("userIDs", userID)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val documentID = document.id

                        Log.d(TAG, "documentID: $documentID")
                        if (documentID != null) {
                            // Retrieve the other user's ID from the conversation document
                            val otherUserIDs = document.get("userIDs") as? List<String>
                            otherUserIDs?.let {
                                val otherUserID = it.find { id -> id != userID }
                                if (otherUserID != null) {
                                    getUserInfo(otherUserID) { name, profileImageUrl ->
                                        val conversation = Conversation(
                                            documentID,
                                            name,
                                            profileImageUrl
                                        )
                                        conversationList.add(conversation)
                                        liveData.postValue(conversationList)
                                    }
                                }
                            }
                        }
                    }
                }
        }

    }

    private fun getUserInfo(userID: String, callback: (name: String, profileImageUrl: String) -> Unit) {
        val usersRef = firestore.collection("users").document(userID)

        usersRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name")
                    val profileImageUrl = documentSnapshot.getString("profileImageUrl")
                    if (name != null && profileImageUrl != null) {
                        callback(name, profileImageUrl)
                    }
                }
            }
    }
}