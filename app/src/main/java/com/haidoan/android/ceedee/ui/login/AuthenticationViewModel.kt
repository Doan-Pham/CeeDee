package com.haidoan.android.ceedee.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuthenticationRepository
    private val isUserSignedIn: MutableLiveData<Boolean>
    private val requiredText: MutableLiveData<String>

    init {
        repository = AuthenticationRepository(application)
        isUserSignedIn = repository.isUserSignedIn()
        requiredText = repository.getRequiredTextMessage()
    }

    fun getRequiredTextMessage(): MutableLiveData<String> {
        return requiredText
    }

    fun isUserSignedIn() = isUserSignedIn

    fun signIn(email: String?, pass: String?) = liveData(Dispatchers.IO) {
        repository.loginFromFireStore(email = email, pass = pass).collect { response ->
            emit(response)
        }
    }

    fun signOut() {
        repository.signOut()
    }
}