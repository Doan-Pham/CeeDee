package com.haidoan.android.ceedee.ui.user_management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.user_management.UserFirestoreDataSource
import com.haidoan.android.ceedee.data.user_management.UserRepository
import com.haidoan.android.ceedee.databinding.FragmentUserManagementBinding


class UserManagementFragment : Fragment() {

    private var _binding: FragmentUserManagementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: UserManagementViewModel by lazy {
        ViewModelProvider(
            this, UserManagementViewModel.Factory(
                UserRepository(UserFirestoreDataSource())
            )
        )[UserManagementViewModel::class.java]
    }

    private lateinit var usersAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerview()
        observeViewModel()
    }

    private fun setupRecyclerview() {
        usersAdapter = UserAdapter()

        binding.apply {
            recyclerviewUsers.adapter = usersAdapter
            recyclerviewUsers.layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            usersAdapter.submitList(
                users
            )
        }
    }
}