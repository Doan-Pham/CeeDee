package com.haidoan.android.ceedee.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.haidoan.android.ceedee.databinding.FragmentLoginCustomerBinding

private const val TAG = "LoginCustomerFragment"

class LoginCustomerFragment : Fragment() {
    private lateinit var binding: FragmentLoginCustomerBinding
    private val authViewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpButtonSendOtp()
    }

    private fun setUpButtonSendOtp() {
        binding.apply {
            buttonSendOtp.setOnClickListener {
                val phoneNumberText = editTextPhoneNumber.text?.trim().toString()
                if (phoneNumberText.isEmpty() || phoneNumberText.length != 9) {
                    editTextPhoneNumber.error =
                        "Please type valid phone number! (No need to type the first 0)"
                    return@setOnClickListener
                }

                toggleProgressBarVisibility(true)
                authViewModel.authenticatePhoneNumber(
                    "+84$phoneNumberText",
                    requireActivity(),
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // This callback will be invoked in two situations:
                            // 1 - Instant verification. In some cases the phone number can be instantly
                            //     verified without needing to send or enter a verification code.
                            // 2 - Auto-retrieval. On some devices Google Play services can automatically
                            //     detect the incoming verification SMS and perform verification without
                            //     user action.
                            Log.d(TAG, "onVerificationCompleted:$credential")
                            authViewModel.signInWithPhoneAuthCredential(
                                requireActivity(),
                                credential
                            )
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            // This callback is invoked in an invalid request for verification is made,
                            // for instance if the the phone number format is not valid.
                            Log.w(TAG, "onVerificationFailed", e)
                            showToastError("Invalid phone number!")
                            toggleProgressBarVisibility(false)

                            if (e is FirebaseAuthInvalidCredentialsException) {
                                showToastError("Invalid phone number!")
                                toggleProgressBarVisibility(false)
                            } else if (e is FirebaseTooManyRequestsException) {
                                // The SMS quota for the project has been exceeded
                                showToastError(" The SMS quota has been exceeded!")
                                toggleProgressBarVisibility(false)
                            }

                            // Show a message and update the UI
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            // The SMS verification code has been sent to the provided phone number, we
                            // now need to ask the user to enter the code and then construct a credential
                            // by combining the code with a verification ID.
                            Log.d(TAG, "onCodeSent:$verificationId")

                            findNavController().navigate(
                                LoginCustomerFragmentDirections.actionLoginCustomerFragmentToPhoneOtpFragment(
                                    verificationId,
                                    "+84$phoneNumberText",
                                )
                            )
                            // Save verification ID and resending token so we can use them later
//                            storedVerificationId = verificationId
//                            resendToken = token
                        }
                    })
            }
        }
    }

    private fun showToastError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    private fun toggleProgressBarVisibility(isShown: Boolean) {
        if (isShown) {
            binding.progressbar.visibility = View.VISIBLE
            binding.constraintLayoutContentWrapper.visibility = View.GONE
        } else {
            binding.progressbar.visibility = View.GONE
            binding.constraintLayoutContentWrapper.visibility = View.VISIBLE
        }
    }
}