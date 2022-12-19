package com.haidoan.android.ceedee.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.haidoan.android.ceedee.databinding.FragmentChooseRoleBinding


class ChooseRoleFragment : Fragment() {
    private lateinit var binding: FragmentChooseRoleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardviewPersonnel.setOnClickListener {
            findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToLoginPersonnelFragment())
        }

    }
}