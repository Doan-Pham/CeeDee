package com.haidoan.android.ceedee.ui.login

import android.app.Application
import android.util.Log

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationRepository(private val application: Application) {
    private val firebaseUserMutableLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()
    private val userLoggedMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val requiredText:  MutableLiveData<String> = MutableLiveData()

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
    }

    fun getRequiredTextMessage():  MutableLiveData<String> {
        return requiredText
    }

    fun getUserLoggedMutableLiveData(): MutableLiveData<Boolean> {
        return userLoggedMutableLiveData
    }

    fun getFirebaseUserMutableLiveData(): MutableLiveData<FirebaseUser?> {
        return firebaseUserMutableLiveData
    }

    fun login(email: String?, pass: String?) {
        auth.signInWithEmailAndPassword(email!!, pass!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseUserMutableLiveData.postValue(auth.currentUser)
                Log.d("TAG", "success")
            } else {
                val networkError = "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
                if (task.exception?.message.toString() == networkError) {
                    requiredText.postValue("A network error has occurred.")
                } else {
                    requiredText.postValue(task.exception?.message.toString())
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        userLoggedMutableLiveData.postValue(true)
    }
}
