package com.example.examen2.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examen2.adapters.KitchenOrderAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.OrderItem
import com.example.examen2.data.SessionManager
import com.example.examen2.databinding.ActivityKitchenBinding
import com.example.examen2.databinding.DialogCancelItemBinding

class KitchenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKitchenBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: KitchenOrderAdapter

    private val areas = arrayOf("TODAS", "COCINA", "BARRA")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKitchenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupSpinner()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupSpinner() {
        val areaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, areas)
        binding.spinnerKitchenArea.adapter = areaAdapter

        binding.spinnerKitchenArea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = KitchenOrderAdapter(
            orderItems = getFilteredKitchenItems(),
            onNextStatusClick = { item -> advanceItemStatus(item) },
            onCancelClick = { item -> handleCancelItem(item) }
        )
        binding.rvKitchenOrders.layoutManager = LinearLayoutManager(this)
        binding.rvKitchenOrders.adapter = adapter
    }

    private fun getFilteredKitchenItems(): List<OrderItem> {
        val areaFilter = binding.spinnerKitchenArea.selectedItem?.toString() ?: "TODAS"
        return dbHelper.getKitchenOrderItems(areaFilter)
    }

    private fun refreshData() {
        adapter.updateData(getFilteredKitchenItems())
    }

    private fun advanceItemStatus(item: OrderItem) {
        val nextStatus = when (item.status) {
            "PENDIENTE" -> "EN_PREPARACION"
            "EN_PREPARACION" -> "LISTO"
            "LISTO" -> "ENTREGADO"
            else -> item.status
        }

        val success = dbHelper.updateOrderItemStatus(item.id, nextStatus)
        if (success) {
            Toast.makeText(this, "Estado actualizado a $nextStatus", Toast.LENGTH_SHORT).show()
            refreshData()
        } else {
            Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCancelItem(item: OrderItem) {
        // REGLA DE NEGOCIO 6: Direct cancellation is ONLY allowed for PENDIENTE status
        // If status is EN_PREPARACION or LISTO, requires ADMIN role!
        if (item.status == "EN_PREPARACION" || item.status == "LISTO") {
            if (sessionManager.getRole() != "ADMIN") {
                AlertDialog.Builder(this)
                    .setTitle("REGLA DE NEGOCIO - Cancelación Bloqueada")
                    .setMessage("No se permite cancelar directamente un producto cuya preparación ya comenzó (${item.status}). Solo el Administrador puede autorizar esta acción.")
                    .setPositiveButton("Entendido", null)
                    .show()
                return
            }
        }

        // Show cancellation dialog prompting for reason
        val dialogBinding = DialogCancelItemBinding.inflate(layoutInflater)
        dialogBinding.tvCancelItemDetails.text = "Mesa #${item.tableNumber} - ${item.quantity}x ${item.productName} (${item.status})"

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Confirmar Cancelación") { dialog, _ ->
                val reason = dialogBinding.etCancelReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Debe especificar el motivo de cancelación", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val canceledBy = sessionManager.getFullName()
                val success = dbHelper.cancelOrderItem(
                    item.id,
                    reason,
                    canceledBy,
                    allowPreparedCancellation = sessionManager.getRole() == "ADMIN"
                )

                if (success) {
                    Toast.makeText(this, "Producto cancelado y registrado en bitácora", Toast.LENGTH_LONG).show()
                    refreshData()
                } else {
                    Toast.makeText(this, "Error al cancelar producto", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Volver", null)
            .show()
    }
}
