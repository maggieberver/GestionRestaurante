package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.R
import com.example.examen2.data.OrderItem
import com.example.examen2.databinding.ItemKitchenOrderBinding

class KitchenOrderAdapter(
    private var orderItems: List<OrderItem>,
    private val onNextStatusClick: (OrderItem) -> Unit,
    private val onCancelClick: (OrderItem) -> Unit
) : RecyclerView.Adapter<KitchenOrderAdapter.KitchenViewHolder>() {

    class KitchenViewHolder(val binding: ItemKitchenOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KitchenViewHolder {
        val binding = ItemKitchenOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KitchenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KitchenViewHolder, position: Int) {
        val item = orderItems[position]
        val context = holder.itemView.context
        with(holder.binding) {
            tvKitchenTable.text = "Mesa #${item.tableNumber}"
            tvKitchenProductName.text = "${item.quantity}x ${item.productName}"
            tvKitchenArea.text = "Área: ${item.area}"

            if (item.specialNotes.isNotEmpty()) {
                tvKitchenNotes.visibility = View.VISIBLE
                tvKitchenNotes.text = "Notas: ${item.specialNotes}"
            } else {
                tvKitchenNotes.visibility = View.GONE
            }

            tvKitchenStatusBadge.text = item.status

            when (item.status) {
                "PENDIENTE" -> {
                    tvKitchenStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.kitchen_pendiente))
                    tvKitchenStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_ocupada_bg))
                    btnNextKitchenStatus.text = "Iniciar Preparación"
                    btnNextKitchenStatus.visibility = View.VISIBLE
                }
                "EN_PREPARACION" -> {
                    tvKitchenStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.kitchen_preparacion))
                    tvKitchenStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_cuenta_bg))
                    btnNextKitchenStatus.text = "Marcar como Listo"
                    btnNextKitchenStatus.visibility = View.VISIBLE
                }
                "LISTO" -> {
                    tvKitchenStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.kitchen_listo))
                    tvKitchenStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_libre_bg))
                    btnNextKitchenStatus.text = "Marcar Entregado"
                    btnNextKitchenStatus.visibility = View.VISIBLE
                }
                else -> { // ENTREGADO / CANCELADO
                    tvKitchenStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    tvKitchenStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_agotado_bg))
                    btnNextKitchenStatus.visibility = View.GONE
                }
            }

            btnNextKitchenStatus.setOnClickListener { onNextStatusClick(item) }
            btnCancelKitchenItem.setOnClickListener { onCancelClick(item) }
        }
    }

    override fun getItemCount(): Int = orderItems.size

    fun updateData(newOrderItems: List<OrderItem>) {
        orderItems = newOrderItems
        notifyDataSetChanged()
    }
}
