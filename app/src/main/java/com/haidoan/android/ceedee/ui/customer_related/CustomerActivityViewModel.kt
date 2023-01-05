package com.haidoan.android.ceedee.ui.customer_related

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import kotlinx.coroutines.launch

private const val TAG = "CustomerActivityVM"

class CustomerActivityViewModel(private val authenticationRepository: AuthenticationRepository) :
    ViewModel() {
    var currentUser = MutableLiveData<FirebaseUser>()
    val isUserSignedIn = authenticationRepository.isUserSignedIn()

    fun resetUser() {
        currentUser.value =
            authenticationRepository.currentUser
        Log.d(TAG, "resetUser() - currentUser: ${currentUser.value}")
    }

    fun signOut() {
        viewModelScope.launch {
            authenticationRepository.signOut()
        }
    }

    class Factory(
        private val authenticationRepository: AuthenticationRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerActivityViewModel(authenticationRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}