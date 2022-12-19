package com.haidoan.android.ceedee

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import kotlinx.coroutines.launch

private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(private val authenticationRepository: AuthenticationRepository) :
    ViewModel() {
    var currentUser = MutableLiveData<User>()

    init {
        viewModelScope.launch {
            currentUser.value = authenticationRepository.getCurrentUserInfo() ?: User()
            Log.d(TAG, "init- currentUser: ${currentUser.value}")
        }
    }

    fun resetUser() {
        viewModelScope.launch {
            currentUser.value = authenticationRepository.getCurrentUserInfo()!!
            Log.d(TAG, "resetUser() - currentUser: ${currentUser.value}")
        }
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
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainActivityViewModel(authenticationRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}