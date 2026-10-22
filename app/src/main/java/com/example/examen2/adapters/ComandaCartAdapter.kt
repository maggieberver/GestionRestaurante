package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.data.OrderItem
import com.example.examen2.databinding.ItemComandaCartBinding

class ComandaCartAdapter(
    private var cartItems: List<OrderItem>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<ComandaCartAdapter.CartViewHolder>() {

    class CartViewHolder(val binding: ItemComandaCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemComandaCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        with(holder.binding) {
            tvCartName.text = "${item.quantity}x ${item.productName}"
            if (item.specialNotes.isNotEmpty()) {
                tvCartNotes.visibility = View.VISIBLE
                tvCartNotes.text = "Notas: ${item.specialNotes}"
            } else {
                tvCartNotes.visibility = View.GONE
            }
            tvCartLineTotal.text = String.format("Importe: $%.2f MXN", item.totalLinePrice)

            btnRemoveCartItem.setOnClickListener {
                onRemoveClick(holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newCartItems: List<OrderItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }
}
