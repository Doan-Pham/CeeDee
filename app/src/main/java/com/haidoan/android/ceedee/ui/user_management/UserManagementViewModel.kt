package com.haidoan.android.ceedee.ui.user_management

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.data.user_management.UserRepository
import kotlinx.coroutines.Dispatchers


class UserManagementViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = liveData(Dispatchers.IO) {
        userRepository.getUsersStream().collect { emit(it) }
    }

    val users: LiveData<List<User>> = _users

    class Factory(
        private val userRepository: UserRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserManagementViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}