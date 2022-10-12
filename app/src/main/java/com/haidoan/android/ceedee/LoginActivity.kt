package com.haidoan.android.ceedee

import android.content.Intent

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import android.view.View

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.haidoan.android.ceedee.databinding.ActivityLoginBinding
import com.haidoan.android.ceedee.ui.login.AuthenticationViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthenticationViewModel

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        authViewModel.getUserData().observe(this, Observer<FirebaseUser?> { _ ->
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        })
        authViewModel.getRequiredTextMessage().observe(this, Observer<String> { s ->
            run {
                setTextRequired(s)
            }
        })

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvMessageRequired.visibility = View.GONE
        setOnClick()
    }

    private fun setOnClick() {
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            val email = binding.edtUsernameLogin.text.toString()
            val pass = binding.edtPasswordLogin.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                setTextRequired("Email or Password cannot be empty")
            } else {
                signIn(email, pass)
            }
            binding.tvMessageRequired.visibility = View.VISIBLE
        })
    }

    private fun setTextRequired(required: String) {
        binding.tvMessageRequired.text = required
    }

    private fun signIn(email: String, pass: String) {
        authViewModel.signIn(email, pass)
    }
}