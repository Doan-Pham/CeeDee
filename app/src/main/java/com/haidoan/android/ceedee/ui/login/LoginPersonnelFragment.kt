package com.haidoan.android.ceedee.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.haidoan.android.ceedee.databinding.FragmentLoginPersonnelBinding
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response


class LoginPersonnelFragment : Fragment() {
    private lateinit var binding: FragmentLoginPersonnelBinding
    private val authViewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginPersonnelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.getRequiredTextMessage().observe(viewLifecycleOwner) { s ->
            run {
                setTextRequired(s)
            }
        }

        binding.tvMessageRequired.visibility = View.INVISIBLE
        setOnClick()
    }

    private fun setOnClick() {
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            val email = binding.edtUsernameLogin.text.toString()
            val pass = binding.edtPasswordLogin.text.toString()

            authViewModel.signIn(email, pass).observe(viewLifecycleOwner) { response ->
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
}