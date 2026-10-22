package com.example.examen2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.databinding.ActivityReportsBinding

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadReports()
    }

    private fun loadReports() {
        val summary = dbHelper.getReportSummary()

        binding.tvReportTotalSales.text = String.format("$%.2f MXN", summary.totalSales)
        binding.tvReportPaidTickets.text = summary.paidTicketsCount.toString()
        binding.tvReportAverageTicket.text = String.format("$%.2f MXN", summary.averageTicket)
        binding.tvReportCancellationsCount.text = summary.totalCancellationsCount.toString()

        // 1. Top Products
        if (summary.topProducts.isEmpty()) {
            binding.tvReportTopProducts.text = "No hay datos de productos vendidos aún."
        } else {
            val sb = StringBuilder()
            summary.topProducts.forEachIndexed { i, p ->
                sb.append(String.format("%d. %-20s  %3dx  $%7.2f MXN\n", i + 1, p.productName.take(20), p.totalQuantity, p.totalAmount))
            }
            binding.tvReportTopProducts.text = sb.toString().trimEnd()
        }

        // 2. Sales by Waiter
        if (summary.salesByWaiter.isEmpty()) {
            binding.tvReportSalesByWaiter.text = "No hay datos de ventas por mesero aún."
        } else {
            val sb = StringBuilder()
            summary.salesByWaiter.forEach { w ->
                sb.append(String.format("• %-18s (%2d tcks)  $%7.2f MXN\n", w.waiterName.take(18), w.ticketCount, w.totalAmount))
            }
            binding.tvReportSalesByWaiter.text = sb.toString().trimEnd()
        }

        // 3. Sales by Payment Method
        if (summary.salesByPaymentMethod.isEmpty()) {
            binding.tvReportSalesByPayment.text = "No hay datos de pagos aún."
        } else {
            val sb = StringBuilder()
            summary.salesByPaymentMethod.forEach { pm ->
                sb.append(String.format("• %-15s (%2d tcks)  $%7.2f MXN\n", pm.paymentMethod, pm.ticketCount, pm.totalAmount))
            }
            binding.tvReportSalesByPayment.text = sb.toString().trimEnd()
        }

        // 4. Cancellation Motives
        if (summary.cancellationMotives.isEmpty()) {
            binding.tvReportCancellationMotives.text = "No se han registrado cancelaciones."
        } else {
            val sb = StringBuilder()
            summary.cancellationMotives.forEach { m ->
                sb.append(String.format("• %-22s (%2dx)  Monto: $%7.2f\n", m.reason.take(22), m.count, m.totalAmount))
            }
            binding.tvReportCancellationMotives.text = sb.toString().trimEnd()
        }
    }
}
