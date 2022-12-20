package com.haidoan.android.ceedee.ui.login

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
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

    fun authenticatePhoneNumber(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        repository.authenticatePhoneNumber(phoneNumber, activity, callbacks)
    }

    fun signInWithPhoneAuthCredential(activity: Activity, credential: PhoneAuthCredential) =
        liveData {
            emit(repository.signInWithPhoneAuthCredential(activity, credential))
        }

    fun signOut() {
        repository.signOut()
    }
}