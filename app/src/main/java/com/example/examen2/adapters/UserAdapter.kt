package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.data.User
import com.example.examen2.databinding.ItemUserBinding

class UserAdapter(
    private var users: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onToggleActive: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding) {
            tvFullName.text = user.fullName
            tvUsername.text = "Usuario: ${user.username}"
            tvRole.text = user.role
            switchActive.isChecked = user.active

            switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggleActive(user, isChecked)
            }

            btnEditUser.setOnClickListener {
                onEditClick(user)
            }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
