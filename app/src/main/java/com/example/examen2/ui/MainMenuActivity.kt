package com.example.examen2.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examen2.data.SessionManager
import com.example.examen2.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupHeaderAndPermissions()
        setupClickListeners()
    }

    private fun setupHeaderAndPermissions() {
        val userName = sessionManager.getFullName()
        val role = sessionManager.getRole()

        binding.tvWelcomeUser.text = "¡Hola, $userName!"
        binding.tvRoleBadge.text = "ROL: $role"

        // Role-based visibility
        when (role) {
            "ADMIN" -> {
                binding.cardMesas.visibility = View.VISIBLE
                binding.cardComandas.visibility = View.VISIBLE
                binding.cardCocina.visibility = View.VISIBLE
                binding.cardCatalogo.visibility = View.VISIBLE
                binding.cardTickets.visibility = View.VISIBLE
                binding.cardReportes.visibility = View.VISIBLE
                binding.cardUsuarios.visibility = View.VISIBLE
                binding.cardCancelaciones.visibility = View.VISIBLE
            }
            "MESERO" -> {
                binding.cardMesas.visibility = View.VISIBLE
                binding.cardComandas.visibility = View.VISIBLE
                binding.cardCocina.visibility = View.GONE
                binding.cardCatalogo.visibility = View.VISIBLE
                binding.cardTickets.visibility = View.GONE
                binding.cardReportes.visibility = View.GONE
                binding.cardUsuarios.visibility = View.GONE
                binding.cardCancelaciones.visibility = View.VISIBLE
            }
            "COCINA" -> {
                binding.cardMesas.visibility = View.GONE
                binding.cardComandas.visibility = View.GONE
                binding.cardCocina.visibility = View.VISIBLE
                binding.cardCatalogo.visibility = View.VISIBLE
                binding.cardTickets.visibility = View.GONE
                binding.cardReportes.visibility = View.GONE
                binding.cardUsuarios.visibility = View.GONE
                binding.cardCancelaciones.visibility = View.VISIBLE
            }
            "CAJERO" -> {
                binding.cardMesas.visibility = View.VISIBLE
                binding.cardComandas.visibility = View.GONE
                binding.cardCocina.visibility = View.GONE
                binding.cardCatalogo.visibility = View.VISIBLE
                binding.cardTickets.visibility = View.VISIBLE
                binding.cardReportes.visibility = View.GONE
                binding.cardUsuarios.visibility = View.GONE
                binding.cardCancelaciones.visibility = View.VISIBLE
            }
            else -> {
                Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNavMesas.setOnClickListener {
            startActivity(Intent(this, TablesActivity::class.java))
        }
        binding.btnNavComandas.setOnClickListener {
            startActivity(Intent(this, ComandaActivity::class.java))
        }
        binding.btnNavCocina.setOnClickListener {
            startActivity(Intent(this, KitchenActivity::class.java))
        }
        binding.btnNavCatalogo.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        binding.btnNavTickets.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
        binding.btnNavReportes.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.btnNavUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }
        binding.btnNavCancelaciones.setOnClickListener {
            startActivity(Intent(this, CancellationsActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
