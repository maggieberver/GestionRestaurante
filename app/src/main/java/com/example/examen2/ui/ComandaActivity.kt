package com.example.examen2.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examen2.adapters.ComandaCartAdapter
import com.example.examen2.adapters.ProductSelectAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.Order
import com.example.examen2.data.OrderItem
import com.example.examen2.data.Product
import com.example.examen2.data.RestaurantTable
import com.example.examen2.data.SessionManager
import com.example.examen2.databinding.ActivityComandaBinding
import com.example.examen2.databinding.DialogAddToCartBinding

class ComandaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComandaBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var catalogAdapter: ProductSelectAdapter
    private lateinit var cartAdapter: ComandaCartAdapter

    private var availableTables = listOf<RestaurantTable>()
    private var selectedTable: RestaurantTable? = null
    private var currentOrder: Order? = null
    private val pendingCartItems = mutableListOf<OrderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComandaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerViews()
        setupTableSpinner()

        val preselectedTableId = intent.getLongExtra("TABLE_ID", -1L)
        if (preselectedTableId != -1L) {
            selectSpinnerTableById(preselectedTableId)
        }

        binding.btnSendComanda.setOnClickListener {
            sendComandaToKitchen()
        }
    }

    private fun setupRecyclerViews() {
        catalogAdapter = ProductSelectAdapter(
            products = dbHelper.getAllProducts(),
            onAddClick = { product -> showAddToCartDialog(product) }
        )
        binding.rvCatalogSelect.layoutManager = LinearLayoutManager(this)
        binding.rvCatalogSelect.adapter = catalogAdapter

        cartAdapter = ComandaCartAdapter(
            cartItems = pendingCartItems,
            onRemoveClick = { index ->
                if (index in pendingCartItems.indices) {
                    pendingCartItems.removeAt(index)
                    cartAdapter.updateData(pendingCartItems)
                    updateSubtotal()
                }
            }
        )
        binding.rvCartItems.layoutManager = LinearLayoutManager(this)
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun setupTableSpinner() {
        availableTables = dbHelper.getAllTables()
        val tableNames = availableTables.map { "Mesa ${it.number} (${it.status})" }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tableNames)
        binding.spinnerTables.adapter = spinnerAdapter

        binding.spinnerTables.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTable = availableTables[position]
                loadTableComandaState()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectSpinnerTableById(tableId: Long) {
        val index = availableTables.indexOfFirst { it.id == tableId }
        if (index >= 0) {
            binding.spinnerTables.setSelection(index)
        }
    }

    private fun loadTableComandaState() {
        val table = selectedTable ?: return
        currentOrder = dbHelper.getOpenOrderForTable(table.id)

        val waiterName = table.assignedWaiterName ?: sessionManager.getFullName()
        val folio = currentOrder?.folio ?: "(Comanda Nueva)"

        binding.tvComandaTableInfo.text = "Mesa #${table.number} | Mesero: $waiterName | Folio: $folio"
        updateSubtotal()
    }

    private fun showAddToCartDialog(product: Product) {
        // Rule 3 Validation: Exhausted products cannot be added
        if (product.status == "AGOTADO") {
            Toast.makeText(this, "REGLA DE NEGOCIO: No se pueden agregar productos agotados a una comanda.", Toast.LENGTH_LONG).show()
            return
        }

        val dialogBinding = DialogAddToCartBinding.inflate(layoutInflater)
        dialogBinding.tvAddToCartTitle.text = "Agregar ${product.name}"

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar a Comanda") { dialog, _ ->
                val qtyStr = dialogBinding.etQuantity.text.toString().trim()
                val qty = qtyStr.toIntOrNull() ?: 1
                val notes = dialogBinding.etSpecialNotes.text.toString().trim()

                if (qty <= 0) {
                    Toast.makeText(this, "Cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val item = OrderItem(
                    orderId = currentOrder?.id ?: 0,
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    quantity = qty,
                    specialNotes = notes,
                    status = "PENDIENTE",
                    area = product.area
                )

                pendingCartItems.add(item)
                cartAdapter.updateData(pendingCartItems)
                updateSubtotal()

                Toast.makeText(this, "${product.name} agregado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateSubtotal() {
        // Calculate existing order items subtotal + pending cart items
        var total = 0.0
        currentOrder?.let { order ->
            val existingItems = dbHelper.getOrderItems(order.id).filter { it.status != "CANCELADO" }
            total += existingItems.sumOf { it.totalLinePrice }
        }
        total += pendingCartItems.sumOf { it.totalLinePrice }

        binding.tvComandaSubtotal.text = String.format("$%.2f MXN", total)
    }

    private fun sendComandaToKitchen() {
        val table = selectedTable ?: run {
            Toast.makeText(this, "Seleccione una mesa", Toast.LENGTH_SHORT).show()
            return
        }

        // Rule 2 Validation: Cannot send empty comanda
        if (pendingCartItems.isEmpty()) {
            Toast.makeText(this, "REGLA DE NEGOCIO: No se puede enviar una comanda vacía. Agregue al menos un producto.", Toast.LENGTH_LONG).show()
            return
        }

        // Rule 4 Validation: Identify table & waiter
        val waiterId = table.assignedWaiterId ?: sessionManager.getUserId()
        val waiterName = table.assignedWaiterName ?: sessionManager.getFullName()

        // Get or Create Order
        var order = currentOrder
        if (order == null) {
            order = dbHelper.createOrder(table.id, table.number, waiterId, waiterName)
            currentOrder = order
        } else {
            // Ensure table service is set to OCUPADA
            dbHelper.openTableService(table.id, waiterId, waiterName, table.dinerCount.coerceAtLeast(1))
        }

        // Insert pending items into DB
        val success = dbHelper.addItemsToOrder(order.id, table.number, pendingCartItems)

        if (success) {
            Toast.makeText(this, "¡Comanda ${order.folio} enviada a cocina con éxito!", Toast.LENGTH_LONG).show()
            pendingCartItems.clear()
            cartAdapter.updateData(pendingCartItems)
            loadTableComandaState()
        } else {
            Toast.makeText(this, "Error al guardar comanda", Toast.LENGTH_SHORT).show()
        }
    }
}
