package com.haidoan.android.ceedee.ui.login

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationRepository(private val application: Application) {
    private val firebaseUserMutableLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()
    private val userLoggedMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
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
            } else {
                Toast.makeText(application, task.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        userLoggedMutableLiveData.postValue(true)
    }
}