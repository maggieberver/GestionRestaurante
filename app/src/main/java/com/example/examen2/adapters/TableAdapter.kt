package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.R
import com.example.examen2.data.RestaurantTable
import com.example.examen2.databinding.ItemTableBinding

class TableAdapter(
    private var tables: List<RestaurantTable>,
    private val onTableClick: (RestaurantTable) -> Unit
) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    class TableViewHolder(val binding: ItemTableBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val table = tables[position]
        val context = holder.itemView.context
        with(holder.binding) {
            tvTableNumber.text = "MESA ${table.number}"
            tvTableZone.text = "Zona: ${table.zone}"
            tvTableCapacity.text = "Capacidad: ${table.capacity} personas"
            tvTableStatusBadge.text = table.status

            when (table.status) {
                "LIBRE" -> {
                    tvTableStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_libre))
                    tvTableStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_libre_bg))
                    tvTableDetails.text = "Mesa libre para servicio"
                }
                "OCUPADA" -> {
                    tvTableStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_ocupada))
                    tvTableStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_ocupada_bg))
                    tvTableDetails.text = "Mesero: ${table.assignedWaiterName ?: "Sin asignar"}\nComensales: ${table.dinerCount}"
                }
                "CUENTA_SOLICITADA" -> {
                    tvTableStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_cuenta))
                    tvTableStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_cuenta_bg))
                    tvTableDetails.text = "Cuenta solicitada\nMesero: ${table.assignedWaiterName ?: "Sin asignar"}"
                }
                else -> { // PAGADA
                    tvTableStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pagada))
                    tvTableStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pagada_bg))
                    tvTableDetails.text = "Cuenta pagada / Pendiente limpieza"
                }
            }

            cardTable.setOnClickListener { onTableClick(table) }
        }
    }

    override fun getItemCount(): Int = tables.size

    fun updateData(newTables: List<RestaurantTable>) {
        tables = newTables
        notifyDataSetChanged()
    }
}
