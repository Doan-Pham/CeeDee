package com.haidoan.android.ceedee.ui.user_management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.databinding.ItemUserBinding


class UserAdapter(private val onButtonDeleteClick: (user: User) -> Unit) :
    ListAdapter<User, UserAdapter.UserViewHolder>(UserUtils()) {

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onButtonDeleteClick: (user: User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                textviewUsername.text = user.username
                textviewPassword.text = user.password
                textviewRole.text = user.role
                buttonDelete.setOnClickListener { onButtonDeleteClick(user) }
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
        return UserViewHolder(binding, onButtonDeleteClick)
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
