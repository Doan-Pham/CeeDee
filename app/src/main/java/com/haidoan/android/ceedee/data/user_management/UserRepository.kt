package com.haidoan.android.ceedee.data.user_management

import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.data.UserRole
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UserRepository(private val firestoreDataSource: UserFirestoreDataSource) {

    fun getUsersStream(): Flow<List<User>> = firestoreDataSource.getUsersStream()

    fun getUserRolesStream(): Flow<List<UserRole>> = firestoreDataSource.getUserRolesStream()

    suspend fun addUser(user: User) = flow {
        emit(Response.Loading())
        emit(Response.Success(firestoreDataSource.addUser(user)))
    }
        .catch { emit(Response.Failure(it.message.toString())) }

}