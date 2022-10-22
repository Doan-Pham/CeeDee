package com.haidoan.android.ceedee.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuthenticationRepository
    private val userData: MutableLiveData<FirebaseUser?>
    private val loggedStatus: MutableLiveData<Boolean>

    private val requiredText: MutableLiveData<String>
    init {
        repository = AuthenticationRepository(application)
        userData = repository.getFirebaseUserMutableLiveData()
        loggedStatus = repository.getUserLoggedMutableLiveData()
        requiredText = repository.getRequiredTextMessage()
    }

    fun getRequiredTextMessage():  MutableLiveData<String> {
        return requiredText
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