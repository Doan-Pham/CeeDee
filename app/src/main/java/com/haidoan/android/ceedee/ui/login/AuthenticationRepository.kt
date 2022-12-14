package com.haidoan.android.ceedee.ui.login

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.data.notification.NotificationRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit


private const val TAG = "AuthenticationRepo"

class AuthenticationRepository {
    private val isUserSignedIn: MutableLiveData<Boolean> = MutableLiveData()
    var currentUser: FirebaseUser? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var authSecond: FirebaseAuth = FirebaseAuth.getInstance()
    private var authDeleteUser: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val defaultFirebaseApp = FirebaseApp.getInstance()

    private val notificationRepository = NotificationRepository()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val requiredText: MutableLiveData<String> = MutableLiveData()

    init {
        auth.addAuthStateListener {
            Log.d(TAG, "authStateListener - currentUser: ${it.currentUser?.uid}")
            currentUser = it.currentUser
            if (currentUser != null) {
                val currentUserUid = it.currentUser?.uid
                isUserSignedIn.postValue(true)
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new FCM registration token
                    val token = task.result
                    scope.launch {
                        if (currentUserUid != null)
                            notificationRepository.addToken(currentUserUid, token)
                    }
                    Log.d(TAG, "init() - Fetch current registration token : $token")
                })
            } else {
                isUserSignedIn.postValue(false)
                job.cancel()
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

    fun loginFromFireStore(inputAuth: FirebaseAuth = auth, email: String?, pass: String?) = flow {
        emit(Response.Loading())
        emit(Response.Success(inputAuth.signInWithEmailAndPassword(email!!, pass!!).await()))
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

        awaitClose { subscription.result }
    }

    suspend fun deleteUser(user: User) {
        authDeleteUser = try {
            val app = FirebaseApp.initializeApp(
                defaultFirebaseApp.applicationContext,
                defaultFirebaseApp.options,
                "DeleteUserAppInstance"
            )
            FirebaseAuth.getInstance(app)
        } catch (e: IllegalStateException) {
            FirebaseAuth.getInstance(FirebaseApp.getInstance("DeleteUserAppInstance"))
        }
        loginFromFireStore(authDeleteUser, user.username, user.password).collect {
            if (it is Response.Success) {
                val userToDelete = authDeleteUser.currentUser
                val credential = EmailAuthProvider
                    .getCredential(user.username, user.password)

                userToDelete!!.reauthenticate(credential)
                    .addOnCompleteListener {
                        userToDelete.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(
                                        TAG,
                                        "User account deleted - uid: ${userToDelete.uid}"
                                    )
                                }
                            }
                    }
            }
        }


    }

    fun signOut() {
        val currentUserId = currentUser?.uid
        scope.launch {
            notificationRepository.deleteToken(currentUserId ?: "")
        }
        auth.signOut()
        isUserSignedIn.postValue(false)
    }

    fun authenticatePhoneNumber(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(activity: Activity, credential: PhoneAuthCredential) =
        auth.signInWithCredential(credential)

}
