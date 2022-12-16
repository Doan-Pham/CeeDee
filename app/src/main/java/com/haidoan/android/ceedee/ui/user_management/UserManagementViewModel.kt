package com.haidoan.android.ceedee.ui.user_management

import android.util.Log
import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.data.UserRole
import com.haidoan.android.ceedee.data.user_management.UserRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "UserManagementViewModel"

class UserManagementViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = liveData(Dispatchers.IO) {
        userRepository.getUsersStream().collect { emit(it) }
    }
    val users: LiveData<List<User>> = _users

    val userRoles: LiveData<List<UserRole>> = liveData(Dispatchers.IO) {
        userRepository.getUserRolesStream().collect { emit(it) }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            authenticationRepository.signUpWithEmailPassword(user.username, user.password)
                .collect { newUserUid ->
                    userRepository.addUser(user.copy(id = newUserUid ?: "")).collect {
                        when (it) {
                            is Response.Success -> Log.d(TAG, "Called addUser: ${it.data}")
                            is Response.Failure -> {}
                            is Response.Loading -> {}
                        }
                    }
                }
        }
    }

    fun deleteUser(user: User) {
        Log.d(TAG, "Called deleteUser - user: $user")
        viewModelScope.launch {
            userRepository.deleteUser(user).collect {}
            authenticationRepository.deleteUser(user)
        }
    }

    class Factory(
        private val authenticationRepository: AuthenticationRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserManagementViewModel(authenticationRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}