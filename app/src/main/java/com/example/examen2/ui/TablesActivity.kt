package com.example.examen2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.examen2.adapters.TableAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.RestaurantTable
import com.example.examen2.data.SessionManager
import com.example.examen2.data.User
import com.example.examen2.databinding.ActivityTablesBinding
import com.example.examen2.databinding.DialogOpenTableBinding

class TablesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTablesBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        adapter = TableAdapter(
            tables = dbHelper.getAllTables(),
            onTableClick = { table -> handleTableClick(table) }
        )
        binding.rvTables.layoutManager = GridLayoutManager(this, 2)
        binding.rvTables.adapter = adapter
    }

    private fun refreshData() {
        adapter.updateData(dbHelper.getAllTables())
    }

    private fun handleTableClick(table: RestaurantTable) {
        if (table.status == "LIBRE") {
            showOpenTableDialog(table)
        } else {
            showOccupiedTableOptions(table)
        }
    }

    private fun showOpenTableDialog(table: RestaurantTable) {
        val dialogBinding = DialogOpenTableBinding.inflate(layoutInflater)
        val waiters = dbHelper.getAllUsers().filter { it.role == "MESERO" || it.role == "ADMIN" }

        if (waiters.isEmpty()) {
            Toast.makeText(this, "No hay meseros disponibles en el sistema", Toast.LENGTH_LONG).show()
            return
        }

        val waiterNames = waiters.map { "${it.fullName} (${it.username})" }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, waiterNames)
        dialogBinding.spinnerWaiters.adapter = spinnerAdapter

        // Pre-select current logged-in user if mesero or admin
        val currentUserId = sessionManager.getUserId()
        val defaultIndex = waiters.indexOfFirst { it.id == currentUserId }.coerceAtLeast(0)
        dialogBinding.spinnerWaiters.setSelection(defaultIndex)

        AlertDialog.Builder(this)
            .setTitle("Mesa ${table.number} - Abrir Servicio")
            .setView(dialogBinding.root)
            .setPositiveButton("Abrir Servicio") { dialog, _ ->
                val dinerStr = dialogBinding.etDinerCount.text.toString().trim()
                val dinerCount = dinerStr.toIntOrNull() ?: 0

                if (dinerCount <= 0) {
                    Toast.makeText(this, "Ingrese un número válido de comensales", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val selectedWaiter = waiters[dialogBinding.spinnerWaiters.selectedItemPosition]
                val success = dbHelper.openTableService(table.id, selectedWaiter.id, selectedWaiter.fullName, dinerCount)

                if (success) {
                    Toast.makeText(this, "Servicio iniciado en Mesa ${table.number}", Toast.LENGTH_SHORT).show()
                    refreshData()
                    // Immediately offer to create comanda
                    val intent = Intent(this, ComandaActivity::class.java).apply {
                        putExtra("TABLE_ID", table.id)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Error al abrir servicio", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showOccupiedTableOptions(table: RestaurantTable) {
        val options = arrayOf(
            "📝 Agregar / Ver Comanda",
            "💳 Cobrar Cuenta / Ticket"
        )

        AlertDialog.Builder(this)
            .setTitle("Mesa ${table.number} (${table.status})")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Check Rule 1: Comanda entry
                        val intent = Intent(this, ComandaActivity::class.java).apply {
                            putExtra("TABLE_ID", table.id)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this, CheckoutActivity::class.java).apply {
                            putExtra("TABLE_ID", table.id)
                        }
                        startActivity(intent)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
}
