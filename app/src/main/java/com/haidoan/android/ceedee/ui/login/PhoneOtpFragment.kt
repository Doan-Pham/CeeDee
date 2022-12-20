package com.haidoan.android.ceedee.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.haidoan.android.ceedee.R
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
        setupEditTextOtp()
        setupButtonVerify()

    }

    private fun setupButtonVerify() {
        binding.buttonVerify.setOnClickListener {
            var inputOtp = ""
            binding.apply {
                inputOtp =
                    otpEditText1.text.toString() + otpEditText2.text.toString() + otpEditText3.text.toString() + otpEditText4.text.toString() + otpEditText5.text.toString() + otpEditText6.text.toString()
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

    private fun setupEditTextOtp() {
        binding.apply {
            otpEditText1.addTextChangedListener(EditTextWatcher(otpEditText1))
            otpEditText2.addTextChangedListener(EditTextWatcher(otpEditText2))
            otpEditText3.addTextChangedListener(EditTextWatcher(otpEditText3))
            otpEditText4.addTextChangedListener(EditTextWatcher(otpEditText4))
            otpEditText5.addTextChangedListener(EditTextWatcher(otpEditText5))
            otpEditText6.addTextChangedListener(EditTextWatcher(otpEditText6))
        }
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {

            val text = p0.toString()
            binding.apply {
                when (view.id) {
                    R.id.otpEditText1 -> if (text.length == 1) otpEditText2.requestFocus()
                    R.id.otpEditText2 -> if (text.length == 1) otpEditText3.requestFocus() else if (text.isEmpty()) otpEditText1.requestFocus()
                    R.id.otpEditText3 -> if (text.length == 1) otpEditText4.requestFocus() else if (text.isEmpty()) otpEditText2.requestFocus()
                    R.id.otpEditText4 -> if (text.length == 1) otpEditText5.requestFocus() else if (text.isEmpty()) otpEditText3.requestFocus()
                    R.id.otpEditText5 -> if (text.length == 1) otpEditText6.requestFocus() else if (text.isEmpty()) otpEditText4.requestFocus()
                    R.id.otpEditText6 -> if (text.isEmpty()) otpEditText5.requestFocus()

                }
            }

        }

    }

    private fun showToastError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }
}