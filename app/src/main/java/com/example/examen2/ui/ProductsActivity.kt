package com.example.examen2.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examen2.adapters.ProductAdapter
import com.example.examen2.data.DatabaseHelper
import com.example.examen2.data.Product
import com.example.examen2.data.SessionManager
import com.example.examen2.databinding.ActivityProductsBinding
import com.example.examen2.databinding.DialogProductBinding

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ProductAdapter

    private val categories = arrayOf("Todas las Categorías", "Hamburguesas", "Tacos", "Ensaladas", "Refrescos", "Bebidas", "Postres", "Cortes")
    private val statuses = arrayOf("Todos los Estados", "DISPONIBLE", "AGOTADO")

    private val dialogCategories = arrayOf("Hamburguesas", "Tacos", "Ensaladas", "Refrescos", "Bebidas", "Postres", "Cortes")
    private val dialogTypes = arrayOf("PLATILLO", "BEBIDA")
    private val dialogAreas = arrayOf("COCINA", "BARRA")
    private val dialogStatuses = arrayOf("DISPONIBLE", "AGOTADO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Hide FAB if user is not admin
        if (sessionManager.getRole() != "ADMIN") {
            binding.fabAddProduct.visibility = View.GONE
        }

        setupSpinners()
        setupRecyclerView()

        binding.fabAddProduct.setOnClickListener {
            showProductDialog(null)
        }
    }

    private fun setupSpinners() {
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = catAdapter

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatusFilter.adapter = statusAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerCategoryFilter.onItemSelectedListener = listener
        binding.spinnerStatusFilter.onItemSelectedListener = listener
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            products = getFilteredProducts(),
            onEditClick = { product ->
                if (sessionManager.getRole() == "ADMIN") {
                    showProductDialog(product)
                } else {
                    Toast.makeText(this, "Solo el administrador puede editar productos", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { product ->
                if (sessionManager.getRole() == "ADMIN") {
                    confirmDeleteProduct(product)
                } else {
                    Toast.makeText(this, "Solo el administrador puede eliminar productos", Toast.LENGTH_SHORT).show()
                }
            },
            onToggleStatusClick = { product ->
                val newStatus = if (product.status == "DISPONIBLE") "AGOTADO" else "DISPONIBLE"
                dbHelper.updateProductStatus(product.id, newStatus)
                Toast.makeText(this, "Estado cambiado a $newStatus", Toast.LENGTH_SHORT).show()
                applyFilter()
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun getFilteredProducts(): List<Product> {
        val selectedCat = binding.spinnerCategoryFilter.selectedItem?.toString() ?: "Todas las Categorías"
        val selectedStatus = binding.spinnerStatusFilter.selectedItem?.toString() ?: "Todos los Estados"

        return dbHelper.getAllProducts().filter { p ->
            val matchCat = selectedCat == "Todas las Categorías" || p.category == selectedCat
            val matchStatus = selectedStatus == "Todos los Estados" || p.status == selectedStatus
            matchCat && matchStatus
        }
    }

    private fun applyFilter() {
        adapter.updateData(getFilteredProducts())
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Desactivar Producto")
            .setMessage("¿Desea eliminar / desactivar el producto '${product.name}'?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                dbHelper.deleteProduct(product.id)
                Toast.makeText(this, "Producto desactivado", Toast.LENGTH_SHORT).show()
                applyFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showProductDialog(productToEdit: Product?) {
        val dialogBinding = DialogProductBinding.inflate(layoutInflater)

        dialogBinding.spinnerDialogCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dialogCategories)
        dialogBinding.spinnerDialogType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dialogTypes)
        dialogBinding.spinnerDialogArea.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dialogAreas)
        dialogBinding.spinnerDialogStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dialogStatuses)

        if (productToEdit != null) {
            dialogBinding.tvDialogTitle.text = "Editar Producto"
            dialogBinding.etDialogProductName.setText(productToEdit.name)
            dialogBinding.etDialogProductDesc.setText(productToEdit.description)
            dialogBinding.etDialogProductPrice.setText(productToEdit.price.toString())

            dialogBinding.spinnerDialogCategory.setSelection(dialogCategories.indexOf(productToEdit.category).coerceAtLeast(0))
            dialogBinding.spinnerDialogType.setSelection(dialogTypes.indexOf(productToEdit.type).coerceAtLeast(0))
            dialogBinding.spinnerDialogArea.setSelection(dialogAreas.indexOf(productToEdit.area).coerceAtLeast(0))
            dialogBinding.spinnerDialogStatus.setSelection(dialogStatuses.indexOf(productToEdit.status).coerceAtLeast(0))
        } else {
            dialogBinding.tvDialogTitle.text = "Agregar Producto"
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { dialog, _ ->
                val name = dialogBinding.etDialogProductName.text.toString().trim()
                val desc = dialogBinding.etDialogProductDesc.text.toString().trim()
                val priceStr = dialogBinding.etDialogProductPrice.text.toString().trim()

                val category = dialogBinding.spinnerDialogCategory.selectedItem.toString()
                val type = dialogBinding.spinnerDialogType.selectedItem.toString()
                val area = dialogBinding.spinnerDialogArea.selectedItem.toString()
                val status = dialogBinding.spinnerDialogStatus.selectedItem.toString()

                if (name.isEmpty() || priceStr.isEmpty()) {
                    Toast.makeText(this, "Nombre y Precio son campos obligatorios", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val price = priceStr.toDoubleOrNull()
                if (price == null || price <= 0) {
                    Toast.makeText(this, "Ingrese un precio válido", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                if (productToEdit == null) {
                    val newProduct = Product(
                        name = name,
                        description = desc,
                        category = category,
                        price = price,
                        type = type,
                        status = status,
                        area = area
                    )
                    dbHelper.insertProduct(newProduct)
                    Toast.makeText(this, "Producto registrado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedProduct = productToEdit.copy(
                        name = name,
                        description = desc,
                        category = category,
                        price = price,
                        type = type,
                        status = status,
                        area = area
                    )
                    dbHelper.updateProduct(updatedProduct)
                    Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                }

                applyFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
