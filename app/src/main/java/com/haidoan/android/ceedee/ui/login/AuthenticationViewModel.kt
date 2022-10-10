package com.haidoan.android.ceedee.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuthenticationRepository
    private val userData: MutableLiveData<FirebaseUser?>
    private val loggedStatus: MutableLiveData<Boolean>

    init {
        repository = AuthenticationRepository(application)
        userData = repository.getFirebaseUserMutableLiveData()
        loggedStatus = repository.getUserLoggedMutableLiveData()
    }

    fun getUserData(): LiveData<FirebaseUser?> {
        return userData
    }

    fun signIn(email: String?, pass: String?) {
        repository.login(email, pass)
    }

    fun signOut() {
        repository.signOut()
    }
}