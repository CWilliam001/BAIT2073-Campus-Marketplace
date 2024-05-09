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

    private val db = Firebase.database
    private lateinit var firestore: FirebaseFirestore
    private val conversationRef = db.getReference("conversations")

    fun retrieveAllConversationItem(context: Context,liveData: MutableLiveData<List<Conversation>>) {
        val conversationList = mutableListOf<Conversation>()
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", null)

        Log.d(TAG, "userID: $userID")

        val query: Query = conversationRef.orderByChild("userIDs").startAt(userID).endAt(userID + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "dataSnapshot: $dataSnapshot")
                for (snapshot in dataSnapshot.children) {
                    val conversationID = snapshot.key
                    Log.d(TAG, "conversationID: $conversationID")
                    val conversation = snapshot.getValue(Conversation::class.java)
                    if (conversationID != null && conversation != null) {
                        val filteredUserIDs = conversation.userID.filter { it != userID }
                        if (filteredUserIDs.isNotEmpty()) {
                            val otherUserID = filteredUserIDs.first()
                            getUserInfo(otherUserID) { name, profileImageUrl ->
                                val conversationItem = Conversation(conversationID, conversation.userID, name, profileImageUrl)
                                conversationList.add(conversationItem)
                                liveData.value = conversationList
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun getUserInfo (userID: String, callback: (String, String) -> Unit) {
        firestore.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                val profileImageUrl = document.getString("profileImageUrl")
                callback(name.toString(), profileImageUrl.toString())
            }
            .addOnFailureListener {exception ->
                Log.w(TAG, "Error getting user document", exception)
            }
    }
}