package com.example.examen2.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examen2.adapters.TicketLineAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.Order
import com.example.examen2.data.OrderItem
import com.example.examen2.data.RestaurantTable
import com.example.examen2.data.SessionManager
import com.example.examen2.databinding.ActivityCheckoutBinding
import com.example.examen2.databinding.DialogReceiptBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TicketLineAdapter

    private var occupiedTables = listOf<RestaurantTable>()
    private var selectedTable: RestaurantTable? = null
    private var currentOrder: Order? = null
    private var orderItems = listOf<OrderItem>()

    private val paymentMethods = arrayOf("EFECTIVO", "TARJETA", "TRANSFERENCIA")

    private var calculatedSubtotal = 0.0
    private var calculatedTax = 0.0
    private var calculatedTip = 0.0
    private var calculatedTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupPaymentMethodSpinner()
        setupTableSpinner()
        setupCalculationListeners()

        val preselectedTableId = intent.getLongExtra("TABLE_ID", -1L)
        if (preselectedTableId != -1L) {
            selectSpinnerTableById(preselectedTableId)
        }

        binding.btnProcessPayment.setOnClickListener {
            processPaymentAndCheckout()
        }
    }

    private fun setupRecyclerView() {
        adapter = TicketLineAdapter(orderItems)
        binding.rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        binding.rvCheckoutItems.adapter = adapter
    }

    private fun setupPaymentMethodSpinner() {
        val payAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentMethods)
        binding.spinnerPaymentMethod.adapter = payAdapter

        binding.spinnerPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val method = paymentMethods[position]
                binding.layoutCashDetails.visibility = if (method == "EFECTIVO") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTableSpinner() {
        occupiedTables = dbHelper.getAllTables().filter { it.status != "LIBRE" }

        if (occupiedTables.isEmpty()) {
            Toast.makeText(this, "No hay mesas ocupadas o con cuentas abiertas", Toast.LENGTH_SHORT).show()
        }

        val tableNames = occupiedTables.map { "Mesa ${it.number} (${it.status})" }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tableNames)
        binding.spinnerCheckoutTables.adapter = spinnerAdapter

        binding.spinnerCheckoutTables.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTable = occupiedTables[position]
                loadTableOrderForCheckout()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectSpinnerTableById(tableId: Long) {
        val index = occupiedTables.indexOfFirst { it.id == tableId }
        if (index >= 0) {
            binding.spinnerCheckoutTables.setSelection(index)
        }
    }

    private fun loadTableOrderForCheckout() {
        val table = selectedTable ?: return
        currentOrder = dbHelper.getOpenOrderForTable(table.id)

        if (currentOrder == null) {
            binding.tvCheckoutInfo.text = "Mesa #${table.number} | Sin comanda activa"
            orderItems = emptyList()
            adapter.updateData(orderItems)
            recalculateTotals()
            return
        }

        val waiterName = currentOrder?.waiterName ?: table.assignedWaiterName ?: "Desconocido"
        binding.tvCheckoutInfo.text = "Mesa #${table.number} | Mesero: $waiterName | Folio: ${currentOrder?.folio}"

        orderItems = dbHelper.getOrderItems(currentOrder!!.id)
        adapter.updateData(orderItems)

        recalculateTotals()
    }

    private fun setupCalculationListeners() {
        binding.cbApplyTax.setOnCheckedChangeListener { _, _ -> recalculateTotals() }

        binding.etTipAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalculateTotals()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etReceivedAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCashChange()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun recalculateTotals() {
        // REGLA DE NEGOCIO 5: Canceled items are excluded from totals
        val validItems = orderItems.filter { it.status != "CANCELADO" }
        calculatedSubtotal = validItems.sumOf { it.totalLinePrice }

        calculatedTax = if (binding.cbApplyTax.isChecked) calculatedSubtotal * 0.16 else 0.0

        val tipStr = binding.etTipAmount.text.toString().trim()
        calculatedTip = tipStr.toDoubleOrNull() ?: 0.0

        calculatedTotal = calculatedSubtotal + calculatedTax + calculatedTip

        binding.tvCheckoutSubtotal.text = String.format("$%.2f MXN", calculatedSubtotal)
        binding.tvCheckoutTax.text = String.format("$%.2f MXN", calculatedTax)
        binding.tvCheckoutTip.text = String.format("$%.2f MXN", calculatedTip)
        binding.tvCheckoutTotal.text = String.format("$%.2f MXN", calculatedTotal)

        updateCashChange()
    }

    private fun updateCashChange() {
        val recStr = binding.etReceivedAmount.text.toString().trim()
        val received = recStr.toDoubleOrNull() ?: 0.0
        val change = if (received >= calculatedTotal) received - calculatedTotal else 0.0
        binding.tvChangeAmount.text = String.format("Cambio: $%.2f MXN", change)
    }

    private fun processPaymentAndCheckout() {
        val table = selectedTable ?: run {
            Toast.makeText(this, "Seleccione una mesa", Toast.LENGTH_SHORT).show()
            return
        }

        val order = currentOrder ?: run {
            Toast.makeText(this, "La mesa no tiene una comanda abierta para cobrar", Toast.LENGTH_LONG).show()
            return
        }

        val validItems = orderItems.filter { it.status != "CANCELADO" }
        if (validItems.isEmpty()) {
            Toast.makeText(this, "No hay productos válidos para cobrar en esta comanda", Toast.LENGTH_LONG).show()
            return
        }

        // REGLA DE NEGOCIO 7: Warning if pending undelivered items exist
        val pendingItems = validItems.filter {
            it.status == "PENDIENTE" ||
                it.status == "EN_PREPARACION" ||
                it.status == "LISTO"
        }
        if (pendingItems.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("⚠️ ADVERTENCIA DE PRODUCTOS PENDIENTES")
                .setMessage("Esta cuenta tiene ${pendingItems.size} producto(s) aún pendientes de entrega en cocina/barra. ¿Desea proceder a cobrar y cerrar de todas formas?")
                .setPositiveButton("Cobrar de todos modos") { _, _ -> executePayment(table, order) }
                .setNegativeButton("Cancelar / Revisar Cocina", null)
                .show()
        } else {
            executePayment(table, order)
        }
    }

    private fun executePayment(table: RestaurantTable, order: Order) {
        val paymentMethod = binding.spinnerPaymentMethod.selectedItem.toString()
        val recStr = binding.etReceivedAmount.text.toString().trim()
        val receivedAmount = if (paymentMethod == "EFECTIVO") (recStr.toDoubleOrNull() ?: calculatedTotal) else calculatedTotal

        if (paymentMethod == "EFECTIVO" && receivedAmount < calculatedTotal) {
            Toast.makeText(this, "El monto recibido es menor al total a pagar", Toast.LENGTH_LONG).show()
            return
        }

        val changeAmount = if (receivedAmount >= calculatedTotal) receivedAmount - calculatedTotal else 0.0
        val waiterName = order.waiterName

        // Execute payment in DB: creates ticket, marks order PAGADA, clears table (REGLA DE NEGOCIO 9)
        val ticket = dbHelper.createTicketAndPay(
            orderId = order.id,
            tableId = table.id,
            tableNumber = table.number,
            waiterName = waiterName,
            subtotal = calculatedSubtotal,
            tax = calculatedTax,
            tip = calculatedTip,
            total = calculatedTotal,
            paymentMethod = paymentMethod,
            receivedAmount = receivedAmount,
            changeAmount = changeAmount
        )

        showReceiptDialog(ticket, orderItems.filter { it.status != "CANCELADO" })
    }

    private fun showReceiptDialog(ticket: com.example.examen2.data.Ticket, items: List<OrderItem>) {
        val dialogBinding = DialogReceiptBinding.inflate(layoutInflater)

        val sb = StringBuilder()
        sb.append("Folio Ticket: ").append(ticket.folio).append("\n")
        sb.append("Fecha/Hora:   ").append(ticket.timestamp).append("\n")
        sb.append("Mesa:         #").append(ticket.tableNumber).append("\n")
        sb.append("Mesero:       ").append(ticket.waiterName).append("\n")
        sb.append("------------------------------------------\n")
        for (it in items) {
            val line = String.format("%-20s x%-2d  $%7.2f\n", it.productName.take(20), it.quantity, it.totalLinePrice)
            sb.append(line)
        }
        sb.append("------------------------------------------\n")
        sb.append(String.format("Subtotal:               $%8.2f\n", ticket.subtotal))
        sb.append(String.format("IVA (16%%):              $%8.2f\n", ticket.tax))
        sb.append(String.format("Propina:                $%8.2f\n", ticket.tip))
        sb.append(String.format("TOTAL:                  $%8.2f\n", ticket.total))
        sb.append("------------------------------------------\n")
        sb.append("Forma de Pago:          ").append(ticket.paymentMethod).append("\n")
        sb.append(String.format("Monto Recibido:         $%8.2f\n", ticket.receivedAmount))
        sb.append(String.format("Cambio:                 $%8.2f\n", ticket.changeAmount))
        sb.append("------------------------------------------\n")
        sb.append("¡Gracias por su consumo!")

        dialogBinding.tvReceiptDetails.text = sb.toString()

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Finalizar y Salir") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
}
