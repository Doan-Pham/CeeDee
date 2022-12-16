package com.haidoan.android.ceedee.data.user_management

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.data.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await

@Suppress("UNCHECKED_CAST")

private const val TAG = "UserFirestoreDataSource"

class UserFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    fun getUsersStream(): Flow<List<User>> =
        firestoreDb.collection("User").snapshots()
            .mapNotNull { it.toObjects(User::class.java).toList() }

    fun getUserRolesStream(): Flow<List<UserRole>> {
        return firestoreDb.collection("UserRole").snapshots()
            .mapNotNull {
                Log.d(TAG, "Called getUserRolesStream: $it")
                it.toObjects(UserRole::class.java).toList()
            }
    }


    suspend fun addUser(
        user: User
    ): DocumentReference? {

        Log.d(TAG, "Called addUser")
        return firestoreDb.collection("User").add(
            mapOf(
                "username" to user.username,
                "password" to user.password,
                "role" to user.role
            )
        ).await()
    }
}