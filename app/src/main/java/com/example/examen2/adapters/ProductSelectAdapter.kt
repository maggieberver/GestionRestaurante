package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.data.Product
import com.example.examen2.databinding.ItemProductSelectBinding

class ProductSelectAdapter(
    private var products: List<Product>,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductSelectAdapter.SelectViewHolder>() {

    class SelectViewHolder(val binding: ItemProductSelectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectViewHolder {
        val binding = ItemProductSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectViewHolder, position: Int) {
        val product = products[position]
        with(holder.binding) {
            tvSelectName.text = product.name
            tvSelectCategoryPrice.text = "${product.category} | $${String.format("%.2f", product.price)} MXN"

            if (product.status == "AGOTADO") {
                tvSelectAgotadoBadge.visibility = View.VISIBLE
                btnAddProductToCart.isEnabled = false
                btnAddProductToCart.alpha = 0.5f
            } else {
                tvSelectAgotadoBadge.visibility = View.GONE
                btnAddProductToCart.isEnabled = true
                btnAddProductToCart.alpha = 1.0f
            }

            btnAddProductToCart.setOnClickListener {
                onAddClick(product)
            }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
