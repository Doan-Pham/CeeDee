package com.haidoan.android.ceedee.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.haidoan.android.ceedee.EXTRA_CURRENT_USER_PHONE_NUMBER
import com.haidoan.android.ceedee.MainActivity
import com.haidoan.android.ceedee.databinding.ActivityLoginBinding

private const val TAG = "AuthenticationActivity"

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthenticationViewModel

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        authViewModel.isUserSignedIn().observe(this) {
            Log.d(TAG, "isUserSignedIn: $it")
            if (it == true) {
                val i = Intent(this, MainActivity::class.java)
                i.putExtra(
                    EXTRA_CURRENT_USER_PHONE_NUMBER,
                    authViewModel.getCurrentUser()?.phoneNumber ?: ""
                )
                Log.d(
                    TAG,
                    "authViewModel.getCurrentUser().value?.phoneNumber : ${authViewModel.getCurrentUser()?.phoneNumber}"
                )
                //i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            }
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}