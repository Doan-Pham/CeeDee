package com.haidoan.android.ceedee.ui.user_management

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.data.UserRole
import com.haidoan.android.ceedee.data.user_management.UserFirestoreDataSource
import com.haidoan.android.ceedee.data.user_management.UserRepository
import com.haidoan.android.ceedee.databinding.FragmentUserManagementBinding
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository

private const val TAG = "UserManagementFragment"

class UserManagementFragment : Fragment() {

    private var _binding: FragmentUserManagementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: UserManagementViewModel by lazy {
        ViewModelProvider(
            this, UserManagementViewModel.Factory(
                AuthenticationRepository(requireActivity().application),
                UserRepository(UserFirestoreDataSource())
            )
        )[UserManagementViewModel::class.java]
    }

    private lateinit var usersAdapter: UserAdapter
    private lateinit var currentSignedInUser: User
    private val roleList = mutableListOf<UserRole>()
    private lateinit var chosenRole: String

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
        setupFab()
        observeViewModel()
    }

    private fun setupFab() {
        binding.apply {
            fabNewUser.setOnClickListener {
                showDialogAddUser()
            }
        }

    }

    private fun setupRecyclerview() {
        usersAdapter = UserAdapter {
            if (currentSignedInUser.id == it.id) {
                Toast.makeText(
                    context,
                    "Can't delete the current signed in user",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                createDialog(message = "Delete this user: ${it.username}?") { _, _ ->
                    viewModel.deleteUser(
                        it
                    )
                }

            }
        }

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

        viewModel.userRoles.observe(viewLifecycleOwner) {
            Log.d(TAG, "userRoles: $it")
            roleList.clear()
            roleList.addAll(it)
        }

        viewModel.currentSignedInUser.observe(viewLifecycleOwner) {
            currentSignedInUser = it ?: User()
        }
    }

    private fun showDialogAddUser() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val editTextEmail = dialogLayout.findViewById<EditText>(R.id.edittext_email)
        val editTextPassword = dialogLayout.findViewById<EditText>(R.id.edittext_password)
        val spinnerRole = dialogLayout.findViewById<Spinner>(R.id.spinner_role)
        spinnerRole.adapter = ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_dropdown_item,
            roleList.map { it.name }
        )
        spinnerRole.onItemSelectedListener =
            object : AdapterView.OnItemClickListener,
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    chosenRole = roleList[position].name
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                }

            }

        AlertDialog.Builder(context)
            .setView(dialogLayout)
            .setPositiveButton("Add") { _, _ ->
                if (editTextEmail.text.toString() == "" ||
                    editTextPassword.text.toString().isEmpty()
                ) {
                    Toast.makeText(
                        requireContext(),
                        "All input fields must be filled!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (editTextPassword.text.toString().trim().length < 6) {
                    Toast.makeText(
                        requireContext(),
                        "Password must be longer than 6 characters",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.addUser(
                        User(
                            "",
                            editTextEmail.text.toString().trim(),
                            editTextPassword.text.toString().trim(),
                            chosenRole
                        )
                    )
                }
                // addGenreToFireStore(editText.text.toString())
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun createDialog(
        title: String = "Confirmation",
        message: String,
        onPositiveButtonClick: DialogInterface.OnClickListener
    ) {
        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Proceed", onPositiveButtonClick)
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
            .show()
    }
}