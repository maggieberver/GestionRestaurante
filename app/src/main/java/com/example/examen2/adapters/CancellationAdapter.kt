package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.data.CancellationLog
import com.example.examen2.databinding.ItemCancellationBinding

class CancellationAdapter(
    private var logs: List<CancellationLog>
) : RecyclerView.Adapter<CancellationAdapter.CancelViewHolder>() {

    class CancelViewHolder(val binding: ItemCancellationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancelViewHolder {
        val binding = ItemCancellationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CancelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CancelViewHolder, position: Int) {
        val log = logs[position]
        with(holder.binding) {
            tvCancelItemTitle.text = "${log.quantity}x ${log.productName}"
            tvCancelAmount.text = String.format("-$%.2f MXN", log.price * log.quantity)
            tvCancelReason.text = "Motivo: ${log.reason}"
            tvCancelMeta.text = "Cancelado por: ${log.canceledBy} | ${log.timestamp}"
        }
    }

    override fun getItemCount(): Int = logs.size

    fun updateData(newLogs: List<CancellationLog>) {
        logs = newLogs
        notifyDataSetChanged()
    }
}
