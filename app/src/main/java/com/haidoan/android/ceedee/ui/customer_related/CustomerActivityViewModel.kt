package com.haidoan.android.ceedee.ui.customer_related

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import kotlinx.coroutines.launch

class CustomerActivityViewModel(private val authenticationRepository: AuthenticationRepository) :
    ViewModel() {
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