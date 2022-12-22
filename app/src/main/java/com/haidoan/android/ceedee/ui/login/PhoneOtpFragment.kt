package com.haidoan.android.ceedee.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.haidoan.android.ceedee.databinding.FragmentPhoneOtpBinding

private const val TAG = "PhoneOtpFragment"

class PhoneOtpFragment : Fragment() {
    private lateinit var binding: FragmentPhoneOtpBinding
    private val authViewModel: AuthenticationViewModel by activityViewModels()
    private val navArgs: PhoneOtpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonVerify()

    }

    private fun setupButtonVerify() {
        binding.buttonVerify.setOnClickListener {
            var inputOtp = ""
            binding.apply {
                inputOtp =
                    editTextOtp.text.toString()
            }

            if (inputOtp.isEmpty() || inputOtp.length != 6) {
                Toast.makeText(context, "Please enter valid OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d(
                TAG,
                "Signing with phone auth credential - verification id: ${navArgs.verificationId}; input otp: $inputOtp"
            )
            val credential = PhoneAuthProvider.getCredential(navArgs.verificationId, inputOtp)
            authViewModel.signInWithPhoneAuthCredential(requireActivity(), credential)
                .observe(viewLifecycleOwner) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        showToastError("The verification code entered was invalid")

                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                        // Update UI
                    }
                }
        }

    }
    private fun showToastError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }
}