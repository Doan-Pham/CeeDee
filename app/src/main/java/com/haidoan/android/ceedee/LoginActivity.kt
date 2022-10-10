package com.haidoan.android.ceedee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        authViewModel.getUserData().observe(this,Observer<FirebaseUser?> { _ ->
               val i = Intent(this, MainActivity::class.java)
               startActivity(i)
        })

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClick()
    }

    private fun setOnClick(){
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            signIn()
        })
    }

    private fun signIn() {
        val email = binding.edtUsernameLogin.text.toString()
        val pass = binding.edtPasswordLogin.text.toString()

        if (email.isNotEmpty() && pass.isNotEmpty()){
            authViewModel.signIn(email,pass)
        }
    }
}