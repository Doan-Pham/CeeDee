package com.haidoan.android.ceedee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.haidoan.android.ceedee.databinding.ActivityLoginBinding
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.login.AuthenticationViewModel

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
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
                //i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                //finish()
            }
        }
        authViewModel.getRequiredTextMessage().observe(
            this,
            Observer<String> { s ->
                run {
                    setTextRequired(s)
                }
            })

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvMessageRequired.visibility = View.INVISIBLE
        setOnClick()

    }

    private fun setOnClick() {
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            val email = binding.edtUsernameLogin.text.toString()
            val pass = binding.edtPasswordLogin.text.toString()

            authViewModel.signIn(email, pass).observe(this) { response ->
                when (response) {
                    is Response.Loading -> {
                        binding.progressbarLogin.visibility = View.VISIBLE
                    }
                    is Response.Success -> {
                        binding.progressbarLogin.visibility = View.INVISIBLE
                    }
                    is Response.Failure -> {
                        binding.progressbarLogin.visibility = View.INVISIBLE
                    }

                }
            }

        })
    }

    private fun setTextRequired(required: String) {
        binding.tvMessageRequired.text = required
        binding.tvMessageRequired.visibility = View.VISIBLE
    }

    private fun signIn(email: String, pass: String) {
        authViewModel.signIn(email, pass)
    }
}