package com.example.examen2.data

data class User(
    val id: Long = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: String, // ADMIN, MESERO, COCINA, CAJERO
    val active: Boolean = true
)

data class Product(
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String, // Hamburguesas, Tacos, Ensaladas, Refrescos, Bebidas, Postres
    val price: Double,
    val type: String, // PLATILLO, BEBIDA
    val status: String, // DISPONIBLE, AGOTADO
    val area: String, // COCINA, BARRA
    val active: Boolean = true
)

data class RestaurantTable(
    val id: Long = 0,
    val number: Int,
    val capacity: Int,
    val zone: String, // Interior, Terraza, V.I.P.
    val status: String, // LIBRE, OCUPADA, CUENTA_SOLICITADA, PAGADA
    val assignedWaiterId: Long? = null,
    val assignedWaiterName: String? = null,
    val dinerCount: Int = 0
)

data class Order(
    val id: Long = 0,
    val folio: String,
    val tableId: Long,
    val tableNumber: Int,
    val waiterId: Long,
    val waiterName: String,
    val timestamp: String,
    val status: String // ABIERTA, PAGADA, CANCELADA
)

data class OrderItem(
    val id: Long = 0,
    val orderId: Long,
    val tableNumber: Int = 0,
    val productId: Long,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val specialNotes: String = "",
    val status: String, // PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
    val area: String, // COCINA, BARRA
    val cancellationReason: String? = null,
    val canceledBy: String? = null
) {
    val totalLinePrice: Double
        get() = price * quantity
}

data class CancellationLog(
    val id: Long = 0,
    val orderId: Long,
    val orderItemId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val reason: String,
    val canceledBy: String,
    val timestamp: String
)

data class Ticket(
    val id: Long = 0,
    val folio: String,
    val orderId: Long,
    val tableId: Long,
    val tableNumber: Int,
    val waiterName: String,
    val timestamp: String,
    val subtotal: Double,
    val tax: Double,
    val tip: Double,
    val total: Double,
    val paymentMethod: String, // EFECTIVO, TARJETA, TRANSFERENCIA
    val paymentStatus: String, // PAGADO
    val receivedAmount: Double,
    val changeAmount: Double
)

data class ProductSales(
    val productName: String,
    val totalQuantity: Int,
    val totalAmount: Double
)

data class WaiterSales(
    val waiterName: String,
    val ticketCount: Int,
    val totalAmount: Double
)

data class PaymentMethodSales(
    val paymentMethod: String,
    val ticketCount: Int,
    val totalAmount: Double
)

data class CancellationMotiveSummary(
    val reason: String,
    val count: Int,
    val totalAmount: Double
)

data class ReportSummary(
    val totalSales: Double,
    val paidTicketsCount: Int,
    val averageTicket: Double,
    val totalCancellationsCount: Int,
    val topProducts: List<ProductSales>,
    val salesByWaiter: List<WaiterSales>,
    val salesByPaymentMethod: List<PaymentMethodSales>,
    val cancellationMotives: List<CancellationMotiveSummary>
)
