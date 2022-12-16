package com.haidoan.android.ceedee.ui.user_management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.data.USER_ROLE_EMPLOYEE
import com.haidoan.android.ceedee.data.USER_ROLE_MANAGER
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.databinding.ItemUserBinding


class UserAdapter : ListAdapter<User, UserAdapter.UserViewHolder>(UserUtils()) {

    class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                textviewUsername.text = user.username
                textviewPassword.text = user.password
                textviewRole.text = when (user.role) {
                    USER_ROLE_EMPLOYEE -> "Employee"
                    USER_ROLE_MANAGER -> "Manager"
                    else -> "UNKNOWN"
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding =
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UserUtils : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }
}
