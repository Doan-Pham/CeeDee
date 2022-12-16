package com.haidoan.android.ceedee.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


private const val TAG = "AuthenticationRepo"

class AuthenticationRepository(private val application: Application) {
    private val isUserSignedIn: MutableLiveData<Boolean> = MutableLiveData()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var authSecond: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val defaultFirebaseApp = FirebaseApp.getInstance()

    private val requiredText: MutableLiveData<String> = MutableLiveData()

    init {
        auth.addAuthStateListener {
            Log.d(TAG, "currentUser: ${it.currentUser?.email}")
            if (it.currentUser != null) {
                isUserSignedIn.postValue(true)
            } else {
                isUserSignedIn.postValue(false)
            }
        }
    }

    fun getRequiredTextMessage(): MutableLiveData<String> {
        return requiredText
    }

    fun isUserSignedIn(): MutableLiveData<Boolean> {
        return isUserSignedIn
    }

    suspend fun getCurrentUserInfo() =
        firestoreDb.collection("User").document(auth.currentUser?.uid ?: "").get().await()
            .toObject(User::class.java)

    fun loginFromFireStore(email: String?, pass: String?) = flow {
        emit(Response.Loading())
        emit(Response.Success(auth.signInWithEmailAndPassword(email!!, pass!!).await()))
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
            val networkError =
                "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
            if (errorMessage == networkError) {
                requiredText.postValue("A network error has occurred.")
            } else {
                requiredText.postValue(errorMessage)
            }
            if (email!!.isEmpty() || pass!!.isEmpty()) {
                requiredText.postValue("Email or Password cannot be empty")
            }
        }
    }

    fun signUpWithEmailPassword(email: String, password: String) = callbackFlow {
        authSecond = try {
            val app = FirebaseApp.initializeApp(
                defaultFirebaseApp.applicationContext,
                defaultFirebaseApp.options,
                "SecondAppInstance"
            )
            FirebaseAuth.getInstance(app)
        } catch (e: IllegalStateException) {
            FirebaseAuth.getInstance(FirebaseApp.getInstance("SecondAppInstance"))
        }
        val subscription = authSecond.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    authSecond.signOut()
                    trySend(task.result.user?.uid)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }

        awaitClose{subscription.result}
    }

    fun signOut() {
        auth.signOut()
        isUserSignedIn.postValue(true)
    }

}
