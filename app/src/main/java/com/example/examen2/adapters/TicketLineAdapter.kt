package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.R
import com.example.examen2.data.OrderItem
import com.example.examen2.databinding.ItemTicketLineBinding

class TicketLineAdapter(
    private var items: List<OrderItem>
) : RecyclerView.Adapter<TicketLineAdapter.TicketViewHolder>() {

    class TicketViewHolder(val binding: ItemTicketLineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        with(holder.binding) {
            tvTicketLineQtyName.text = "${item.quantity}x ${item.productName}"
            tvTicketLineStatus.text = item.status
            tvTicketLineTotal.text = String.format("$%.2f", item.totalLinePrice)

            if (item.status == "CANCELADO") {
                tvTicketLineStatus.setTextColor(ContextCompat.getColor(context, R.color.kitchen_cancelado))
                tvTicketLineQtyName.paintFlags = tvTicketLineQtyName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTicketLineStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                tvTicketLineQtyName.paintFlags = tvTicketLineQtyName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<OrderItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
