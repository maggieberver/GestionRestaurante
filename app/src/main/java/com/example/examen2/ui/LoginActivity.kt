package com.example.examen2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.SessionManager
import com.example.examen2.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupQuickButtons()

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = dbHelper.authenticateUser(username, password)
            if (user != null) {
                if (!user.active) {
                    Toast.makeText(this, "Este usuario se encuentra desactivado", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                sessionManager.saveUserSession(user)
                Toast.makeText(this, "¡Bienvenido, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                Toast.makeText(this, "Credenciales incorrectas. Verifique e intente de nuevo.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupQuickButtons() {
        binding.btnQuickAdmin.setOnClickListener {
            binding.etUsername.setText("admin")
            binding.etPassword.setText("123")
        }
        binding.btnQuickMesero.setOnClickListener {
            binding.etUsername.setText("mesero1")
            binding.etPassword.setText("123")
        }
        binding.btnQuickCocina.setOnClickListener {
            binding.etUsername.setText("cocina1")
            binding.etPassword.setText("123")
        }
        binding.btnQuickCajero.setOnClickListener {
            binding.etUsername.setText("cajero1")
            binding.etPassword.setText("123")
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
