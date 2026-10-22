package com.example.examen2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.examen2.R
import com.example.examen2.data.Product
import com.example.examen2.databinding.ItemProductBinding

class ProductAdapter(
    private var products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
    private val onToggleStatusClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context
        with(holder.binding) {
            tvProductName.text = product.name
            tvProductPrice.text = String.format("$%.2f MXN", product.price)
            tvProductDesc.text = product.description
            tvCategoryTag.text = product.category
            tvAreaTag.text = "Área: ${product.area}"

            btnToggleStatus.text = product.status
            if (product.status == "DISPONIBLE") {
                btnToggleStatus.setTextColor(ContextCompat.getColor(context, R.color.status_disponible))
            } else {
                btnToggleStatus.setTextColor(ContextCompat.getColor(context, R.color.status_agotado))
            }

            btnToggleStatus.setOnClickListener { onToggleStatusClick(product) }
            btnEditProduct.setOnClickListener { onEditClick(product) }
            btnDeleteProduct.setOnClickListener { onDeleteClick(product) }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
