package com.haidoan.android.ceedee.ui.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthenticationRepository(private val application: Application) {
    private val firebaseUserMutableLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()
    private val userLoggedMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val requiredText: MutableLiveData<String> = MutableLiveData()

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
    }

    fun getRequiredTextMessage(): MutableLiveData<String> {
        return requiredText
    }

    fun getUserLoggedMutableLiveData(): MutableLiveData<Boolean> {
        return userLoggedMutableLiveData
    }

    fun getFirebaseUserMutableLiveData(): MutableLiveData<FirebaseUser?> {
        return firebaseUserMutableLiveData
    }

    suspend fun getCurrentUserInfo() =
        firestoreDb.collection("User").document(auth.currentUser?.uid ?: "").get().await()
            .toObject(User::class.java)

//    suspend fun getUserInfoById(userId: String) =
//        firestoreDb.collection("User").document(userId).get().await().toObject(User::class.java)


    fun loginFromFireStore(email: String?, pass: String?) = flow {
        emit(Response.Loading())
        emit(Response.Success(auth.signInWithEmailAndPassword(email!!, pass!!).await().run {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }))
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
            val networkError =
                "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
            if (errorMessage == networkError) {
                requiredText.postValue("A network error has occurred.")
            } else {
                requiredText.postValue(errorMessage)
            }
            if (email!!.isEmpty() || pass!!.isEmpty()) {
                requiredText.postValue("Email or Password cannot be empty")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        userLoggedMutableLiveData.postValue(true)
    }
}
