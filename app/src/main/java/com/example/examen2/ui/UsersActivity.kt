package com.example.examen2.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examen2.adapters.UserAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.User
import com.example.examen2.databinding.ActivityUsersBinding
import com.example.examen2.databinding.DialogUserBinding

class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: UserAdapter
    private val roles = arrayOf("ADMIN", "MESERO", "COCINA", "CAJERO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        binding.fabAddUser.setOnClickListener {
            showUserDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            users = dbHelper.getAllUsers(),
            onEditClick = { user -> showUserDialog(user) },
            onToggleActive = { user, active ->
                dbHelper.toggleUserActive(user.id, active)
                Toast.makeText(this, "Estado de usuario actualizado", Toast.LENGTH_SHORT).show()
                refreshData()
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    private fun refreshData() {
        adapter.updateData(dbHelper.getAllUsers())
    }

    private fun showUserDialog(userToEdit: User?) {
        val dialogBinding = DialogUserBinding.inflate(layoutInflater)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRole.adapter = spinnerAdapter

        if (userToEdit != null) {
            dialogBinding.tvDialogTitle.text = "Editar Usuario"
            dialogBinding.etDialogFullName.setText(userToEdit.fullName)
            dialogBinding.etDialogUsername.setText(userToEdit.username)
            dialogBinding.etDialogPassword.setText(userToEdit.password)
            val roleIndex = roles.indexOf(userToEdit.role)
            if (roleIndex >= 0) dialogBinding.spinnerRole.setSelection(roleIndex)
        } else {
            dialogBinding.tvDialogTitle.text = "Agregar Usuario"
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { dialog, _ ->
                val fullName = dialogBinding.etDialogFullName.text.toString().trim()
                val username = dialogBinding.etDialogUsername.text.toString().trim()
                val password = dialogBinding.etDialogPassword.text.toString().trim()
                val role = dialogBinding.spinnerRole.selectedItem.toString()

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                if (userToEdit == null) {
                    val newUser = User(username = username, password = password, fullName = fullName, role = role)
                    val result = dbHelper.insertUser(newUser)
                    if (result > 0) {
                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error o usuario ya existente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val updatedUser = userToEdit.copy(username = username, password = password, fullName = fullName, role = role)
                    val success = dbHelper.updateUser(updatedUser)
                    if (success) {
                        Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
                refreshData()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
