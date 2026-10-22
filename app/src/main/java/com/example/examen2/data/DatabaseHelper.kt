package com.example.examen2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "restaurante.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        private const val TABLE_USERS = "users"
        private const val TABLE_PRODUCTS = "products"
        private const val TABLE_RESTAURANT_TABLES = "restaurant_tables"
        private const val TABLE_ORDERS = "orders"
        private const val TABLE_ORDER_ITEMS = "order_items"
        private const val TABLE_CANCELLATION_LOGS = "cancellation_logs"
        private const val TABLE_TICKETS = "tickets"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users Table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL,
                active INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // Create Products Table
        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                category TEXT NOT NULL,
                price REAL NOT NULL,
                type TEXT NOT NULL,
                status TEXT NOT NULL,
                area TEXT NOT NULL,
                active INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // Create Restaurant Tables
        db.execSQL("""
            CREATE TABLE $TABLE_RESTAURANT_TABLES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                number INTEGER UNIQUE NOT NULL,
                capacity INTEGER NOT NULL,
                zone TEXT NOT NULL,
                status TEXT NOT NULL,
                assigned_waiter_id INTEGER,
                assigned_waiter_name TEXT,
                diner_count INTEGER DEFAULT 0
            )
        """.trimIndent())

        // Create Orders Table
        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                folio TEXT UNIQUE NOT NULL,
                table_id INTEGER NOT NULL,
                table_number INTEGER NOT NULL,
                waiter_id INTEGER NOT NULL,
                waiter_name TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                status TEXT NOT NULL
            )
        """.trimIndent())

        // Create Order Items Table
        db.execSQL("""
            CREATE TABLE $TABLE_ORDER_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                table_number INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                price REAL NOT NULL,
                quantity INTEGER NOT NULL,
                special_notes TEXT,
                status TEXT NOT NULL,
                area TEXT NOT NULL,
                cancellation_reason TEXT,
                canceled_by TEXT
            )
        """.trimIndent())

        // Create Cancellation Logs Table
        db.execSQL("""
            CREATE TABLE $TABLE_CANCELLATION_LOGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                order_item_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                reason TEXT NOT NULL,
                canceled_by TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
        """.trimIndent())

        // Create Tickets Table
        db.execSQL("""
            CREATE TABLE $TABLE_TICKETS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                folio TEXT UNIQUE NOT NULL,
                order_id INTEGER NOT NULL,
                table_id INTEGER NOT NULL,
                table_number INTEGER NOT NULL,
                waiter_name TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                subtotal REAL NOT NULL,
                tax REAL NOT NULL,
                tip REAL NOT NULL,
                total REAL NOT NULL,
                payment_method TEXT NOT NULL,
                payment_status TEXT NOT NULL,
                received_amount REAL NOT NULL,
                change_amount REAL NOT NULL
            )
        """.trimIndent())

        // Seed initial data
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TICKETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CANCELLATION_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESTAURANT_TABLES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // 1. Seed Users
        val users = listOf(
            User(username = "admin", password = "123", fullName = "Administrador General", role = "ADMIN"),
            User(username = "mesero1", password = "123", fullName = "Juan Pérez (Mesero)", role = "MESERO"),
            User(username = "cocina1", password = "123", fullName = "María Chef (Cocina)", role = "COCINA"),
            User(username = "cajero1", password = "123", fullName = "Carlos López (Cajero)", role = "CAJERO")
        )
        for (u in users) {
            val cv = ContentValues().apply {
                put("username", u.username)
                put("password", u.password)
                put("full_name", u.fullName)
                put("role", u.role)
                put("active", 1)
            }
            db.insert(TABLE_USERS, null, cv)
        }

        // 2. Seed Products
        val products = listOf(
            Product(name = "Hamburguesa Clásica", description = "Carne 150g, queso cheddar, lechuga y tomate", category = "Hamburguesas", price = 120.0, type = "PLATILLO", status = "DISPONIBLE", area = "COCINA"),
            Product(name = "Hamburguesa BBQ Bacon", description = "Carne 180g, tocino crocante y salsa BBQ", category = "Hamburguesas", price = 145.0, type = "PLATILLO", status = "DISPONIBLE", area = "COCINA"),
            Product(name = "Tacos al Pastor (5 pzas)", description = "Tortilla de maíz, carne al pastor con piña y cilantro", category = "Tacos", price = 95.0, type = "PLATILLO", status = "DISPONIBLE", area = "COCINA"),
            Product(name = "Tacos de Sirloin (4 pzas)", description = "Sirloin marinado con cebollitas asadas", category = "Tacos", price = 135.0, type = "PLATILLO", status = "DISPONIBLE", area = "COCINA"),
            Product(name = "Ensalada César con Pollo", description = "Pechuga a la plancha, crutones, queso parmesano", category = "Ensaladas", price = 110.0, type = "PLATILLO", status = "DISPONIBLE", area = "COCINA"),
            Product(name = "Refresco Cola 600ml", description = "Lata o botella bien fría", category = "Refrescos", price = 35.0, type = "BEBIDA", status = "DISPONIBLE", area = "BARRA"),
            Product(name = "Agua de Horchata 500ml", description = "Agua fresca tradicional hecha en casa", category = "Bebidas", price = 30.0, type = "BEBIDA", status = "DISPONIBLE", area = "BARRA"),
            Product(name = "Cerveza Nacional 355ml", description = "Cerveza clara u oscura", category = "Bebidas", price = 50.0, type = "BEBIDA", status = "DISPONIBLE", area = "BARRA"),
            Product(name = "Pastel de Chocolate", description = "Rebanada con fudge de chocolate amargo", category = "Postres", price = 65.0, type = "PLATILLO", status = "DISPONIBLE", area = "COCINA"),
            // Sample AGOTADO product for testing business rules!
            Product(name = "Corte Rib Eye 300g (Agotado)", description = "Corte certificado en término a elegir", category = "Cortes", price = 290.0, type = "PLATILLO", status = "AGOTADO", area = "COCINA")
        )
        for (p in products) {
            val cv = ContentValues().apply {
                put("name", p.name)
                put("description", p.description)
                put("category", p.category)
                put("price", p.price)
                put("type", p.type)
                put("status", p.status)
                put("area", p.area)
                put("active", 1)
            }
            db.insert(TABLE_PRODUCTS, null, cv)
        }

        // 3. Seed Tables
        val tables = listOf(
            RestaurantTable(number = 1, capacity = 2, zone = "Interior", status = "LIBRE"),
            RestaurantTable(number = 2, capacity = 4, zone = "Interior", status = "LIBRE"),
            RestaurantTable(number = 3, capacity = 4, zone = "Interior", status = "LIBRE"),
            RestaurantTable(number = 4, capacity = 6, zone = "Terraza", status = "LIBRE"),
            RestaurantTable(number = 5, capacity = 8, zone = "Terraza", status = "LIBRE"),
            RestaurantTable(number = 6, capacity = 4, zone = "V.I.P.", status = "LIBRE")
        )
        for (t in tables) {
            val cv = ContentValues().apply {
                put("number", t.number)
                put("capacity", t.capacity)
                put("zone", t.zone)
                put("status", t.status)
                put("assigned_waiter_id", null as Long?)
                put("assigned_waiter_name", null as String?)
                put("diner_count", 0)
            }
            db.insert(TABLE_RESTAURANT_TABLES, null, cv)
        }
    }

    // ==========================================
    // USER MODULE OPERATIONS
    // ==========================================
    fun authenticateUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "username = ? AND password = ? AND active = 1",
            arrayOf(username, password),
            null, null, null
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
                active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
            )
        }
        cursor.close()
        return user
    }

    fun getAllUsers(): List<User> {
        val list = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, null, null, null, null, null, "full_name ASC")
        while (cursor.moveToNext()) {
            list.add(
                User(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                    role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
                    active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
                )
            )
        }
        cursor.close()
        return list
    }

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("full_name", user.fullName)
            put("role", user.role)
            put("active", if (user.active) 1 else 0)
        }
        return db.insert(TABLE_USERS, null, cv)
    }

    fun updateUser(user: User): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("full_name", user.fullName)
            put("role", user.role)
            put("active", if (user.active) 1 else 0)
        }
        return db.update(TABLE_USERS, cv, "id = ?", arrayOf(user.id.toString())) > 0
    }

    fun toggleUserActive(userId: Long, active: Boolean): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("active", if (active) 1 else 0)
        }
        return db.update(TABLE_USERS, cv, "id = ?", arrayOf(userId.toString())) > 0
    }

    // ==========================================
    // PRODUCT CATALOG MODULE OPERATIONS
    // ==========================================
    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, "active = 1", null, null, null, "category ASC, name ASC")
        while (cursor.moveToNext()) {
            list.add(
                Product(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    area = cursor.getString(cursor.getColumnIndexOrThrow("area")),
                    active = cursor.getInt(cursor.getColumnIndexOrThrow("active")) == 1
                )
            )
        }
        cursor.close()
        return list
    }

    fun insertProduct(product: Product): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("name", product.name)
            put("description", product.description)
            put("category", product.category)
            put("price", product.price)
            put("type", product.type)
            put("status", product.status)
            put("area", product.area)
            put("active", 1)
        }
        return db.insert(TABLE_PRODUCTS, null, cv)
    }

    fun updateProduct(product: Product): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("name", product.name)
            put("description", product.description)
            put("category", product.category)
            put("price", product.price)
            put("type", product.type)
            put("status", product.status)
            put("area", product.area)
        }
        return db.update(TABLE_PRODUCTS, cv, "id = ?", arrayOf(product.id.toString())) > 0
    }

    fun updateProductStatus(productId: Long, status: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("status", status)
        }
        return db.update(TABLE_PRODUCTS, cv, "id = ?", arrayOf(productId.toString())) > 0
    }

    fun deleteProduct(productId: Long): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("active", 0)
        }
        return db.update(TABLE_PRODUCTS, cv, "id = ?", arrayOf(productId.toString())) > 0
    }

    // ==========================================
    // TABLES MODULE OPERATIONS
    // ==========================================
    fun getAllTables(): List<RestaurantTable> {
        val list = mutableListOf<RestaurantTable>()
        val db = readableDatabase
        val cursor = db.query(TABLE_RESTAURANT_TABLES, null, null, null, null, null, "number ASC")
        while (cursor.moveToNext()) {
            list.add(
                RestaurantTable(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    number = cursor.getInt(cursor.getColumnIndexOrThrow("number")),
                    capacity = cursor.getInt(cursor.getColumnIndexOrThrow("capacity")),
                    zone = cursor.getString(cursor.getColumnIndexOrThrow("zone")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    assignedWaiterId = if (cursor.isNull(cursor.getColumnIndexOrThrow("assigned_waiter_id"))) null else cursor.getLong(cursor.getColumnIndexOrThrow("assigned_waiter_id")),
                    assignedWaiterName = cursor.getString(cursor.getColumnIndexOrThrow("assigned_waiter_name")),
                    dinerCount = cursor.getInt(cursor.getColumnIndexOrThrow("diner_count"))
                )
            )
        }
        cursor.close()
        return list
    }

    fun getTableById(tableId: Long): RestaurantTable? {
        val db = readableDatabase
        val cursor = db.query(TABLE_RESTAURANT_TABLES, null, "id = ?", arrayOf(tableId.toString()), null, null, null)
        var table: RestaurantTable? = null
        if (cursor.moveToFirst()) {
            table = RestaurantTable(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                number = cursor.getInt(cursor.getColumnIndexOrThrow("number")),
                capacity = cursor.getInt(cursor.getColumnIndexOrThrow("capacity")),
                zone = cursor.getString(cursor.getColumnIndexOrThrow("zone")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                assignedWaiterId = if (cursor.isNull(cursor.getColumnIndexOrThrow("assigned_waiter_id"))) null else cursor.getLong(cursor.getColumnIndexOrThrow("assigned_waiter_id")),
                assignedWaiterName = cursor.getString(cursor.getColumnIndexOrThrow("assigned_waiter_name")),
                dinerCount = cursor.getInt(cursor.getColumnIndexOrThrow("diner_count"))
            )
        }
        cursor.close()
        return table
    }

    fun openTableService(tableId: Long, waiterId: Long, waiterName: String, dinerCount: Int): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("status", "OCUPADA")
            put("assigned_waiter_id", waiterId)
            put("assigned_waiter_name", waiterName)
            put("diner_count", dinerCount)
        }
        return db.update(
            TABLE_RESTAURANT_TABLES,
            cv,
            "id = ? AND status = 'LIBRE'",
            arrayOf(tableId.toString())
        ) > 0
    }

    fun updateTableStatus(tableId: Long, status: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("status", status)
        }
        return db.update(TABLE_RESTAURANT_TABLES, cv, "id = ?", arrayOf(tableId.toString())) > 0
    }

    fun clearTable(tableId: Long): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("status", "LIBRE")
            putNull("assigned_waiter_id")
            putNull("assigned_waiter_name")
            put("diner_count", 0)
        }
        return db.update(TABLE_RESTAURANT_TABLES, cv, "id = ?", arrayOf(tableId.toString())) > 0
    }

    // ==========================================
    // COMANDAS / ORDERS & ITEMS MODULE OPERATIONS
    // ==========================================
    fun getOpenOrderForTable(tableId: Long): Order? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ORDERS,
            null,
            "table_id = ? AND status = 'ABIERTA'",
            arrayOf(tableId.toString()),
            null, null, "id DESC"
        )
        var order: Order? = null
        if (cursor.moveToFirst()) {
            order = Order(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                folio = cursor.getString(cursor.getColumnIndexOrThrow("folio")),
                tableId = cursor.getLong(cursor.getColumnIndexOrThrow("table_id")),
                tableNumber = cursor.getInt(cursor.getColumnIndexOrThrow("table_number")),
                waiterId = cursor.getLong(cursor.getColumnIndexOrThrow("waiter_id")),
                waiterName = cursor.getString(cursor.getColumnIndexOrThrow("waiter_name")),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            )
        }
        cursor.close()
        return order
    }

    fun createOrder(tableId: Long, tableNumber: Int, waiterId: Long, waiterName: String): Order {
        val db = writableDatabase
        getOpenOrderForTable(tableId)?.let { return it }

        val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val count = db.compileStatement("SELECT COUNT(*) FROM $TABLE_ORDERS").simpleQueryForLong()
        val folio = "COM-${1000 + count + 1}"

        val cv = ContentValues().apply {
            put("folio", folio)
            put("table_id", tableId)
            put("table_number", tableNumber)
            put("waiter_id", waiterId)
            put("waiter_name", waiterName)
            put("timestamp", now)
            put("status", "ABIERTA")
        }
        val orderId = db.insert(TABLE_ORDERS, null, cv)

        // Mark table as occupied
        updateTableStatus(tableId, "OCUPADA")

        return Order(
            id = orderId,
            folio = folio,
            tableId = tableId,
            tableNumber = tableNumber,
            waiterId = waiterId,
            waiterName = waiterName,
            timestamp = now,
            status = "ABIERTA"
        )
    }

    fun addItemsToOrder(orderId: Long, tableNumber: Int, items: List<OrderItem>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (item in items) {
                val cv = ContentValues().apply {
                    put("order_id", orderId)
                    put("table_number", tableNumber)
                    put("product_id", item.productId)
                    put("product_name", item.productName)
                    put("price", item.price)
                    put("quantity", item.quantity)
                    put("special_notes", item.specialNotes)
                    put("status", "PENDIENTE")
                    put("area", item.area)
                }
                db.insert(TABLE_ORDER_ITEMS, null, cv)
            }
            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun getOrderItems(orderId: Long): List<OrderItem> {
        val list = mutableListOf<OrderItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ORDER_ITEMS,
            null,
            "order_id = ?",
            arrayOf(orderId.toString()),
            null, null, "id ASC"
        )
        while (cursor.moveToNext()) {
            list.add(
                OrderItem(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id")),
                    tableNumber = cursor.getInt(cursor.getColumnIndexOrThrow("table_number")),
                    productId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id")),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    specialNotes = cursor.getString(cursor.getColumnIndexOrThrow("special_notes")) ?: "",
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    area = cursor.getString(cursor.getColumnIndexOrThrow("area")),
                    cancellationReason = cursor.getString(cursor.getColumnIndexOrThrow("cancellation_reason")),
                    canceledBy = cursor.getString(cursor.getColumnIndexOrThrow("canceled_by"))
                )
            )
        }
        cursor.close()
        return list
    }

    // ==========================================
    // KITCHEN MODULE OPERATIONS
    // ==========================================
    fun getKitchenOrderItems(areaFilter: String? = null): List<OrderItem> {
        val list = mutableListOf<OrderItem>()
        val db = readableDatabase
        val selection = if (areaFilter.isNullOrEmpty() || areaFilter == "TODAS") {
            "status IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO')"
        } else {
            "status IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO') AND area = ?"
        }
        val selectionArgs = if (areaFilter.isNullOrEmpty() || areaFilter == "TODAS") null else arrayOf(areaFilter)

        val cursor = db.query(TABLE_ORDER_ITEMS, null, selection, selectionArgs, null, null, "id ASC")
        while (cursor.moveToNext()) {
            list.add(
                OrderItem(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id")),
                    tableNumber = cursor.getInt(cursor.getColumnIndexOrThrow("table_number")),
                    productId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id")),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    specialNotes = cursor.getString(cursor.getColumnIndexOrThrow("special_notes")) ?: "",
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    area = cursor.getString(cursor.getColumnIndexOrThrow("area")),
                    cancellationReason = cursor.getString(cursor.getColumnIndexOrThrow("cancellation_reason")),
                    canceledBy = cursor.getString(cursor.getColumnIndexOrThrow("canceled_by"))
                )
            )
        }
        cursor.close()
        return list
    }

    fun updateOrderItemStatus(itemId: Long, newStatus: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("status", newStatus)
        }
        return db.update(TABLE_ORDER_ITEMS, cv, "id = ?", arrayOf(itemId.toString())) > 0
    }

    // ==========================================
    // CANCELLATION MODULE OPERATIONS
    // ==========================================
    fun cancelOrderItem(
        itemId: Long,
        reason: String,
        canceledBy: String,
        allowPreparedCancellation: Boolean = false
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // 1. Get item details
            val cursor = db.query(TABLE_ORDER_ITEMS, null, "id = ?", arrayOf(itemId.toString()), null, null, null)
            if (!cursor.moveToFirst()) {
                cursor.close()
                return false
            }
            val orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id"))
            val productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"))
            val price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
            val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            cursor.close()

            if (
                status == "CANCELADO" ||
                (status != "PENDIENTE" && !allowPreparedCancellation)
            ) {
                return false
            }

            // 2. Update item status to CANCELADO
            val cvItem = ContentValues().apply {
                put("status", "CANCELADO")
                put("cancellation_reason", reason)
                put("canceled_by", canceledBy)
            }
            db.update(TABLE_ORDER_ITEMS, cvItem, "id = ?", arrayOf(itemId.toString()))

            // 3. Log cancellation
            val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val cvLog = ContentValues().apply {
                put("order_id", orderId)
                put("order_item_id", itemId)
                put("product_name", productName)
                put("quantity", quantity)
                put("price", price)
                put("reason", reason)
                put("canceled_by", canceledBy)
                put("timestamp", now)
            }
            db.insert(TABLE_CANCELLATION_LOGS, null, cvLog)

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun getAllCancellations(): List<CancellationLog> {
        val list = mutableListOf<CancellationLog>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CANCELLATION_LOGS, null, null, null, null, null, "id DESC")
        while (cursor.moveToNext()) {
            list.add(
                CancellationLog(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id")),
                    orderItemId = cursor.getLong(cursor.getColumnIndexOrThrow("order_item_id")),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    reason = cursor.getString(cursor.getColumnIndexOrThrow("reason")),
                    canceledBy = cursor.getString(cursor.getColumnIndexOrThrow("canceled_by")),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                )
            )
        }
        cursor.close()
        return list
    }

    // ==========================================
    // TICKETS & PAYMENT MODULE OPERATIONS
    // ==========================================
    fun createTicketAndPay(
        orderId: Long,
        tableId: Long,
        tableNumber: Int,
        waiterName: String,
        subtotal: Double,
        tax: Double,
        tip: Double,
        total: Double,
        paymentMethod: String,
        receivedAmount: Double,
        changeAmount: Double
    ): Ticket {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val count = db.compileStatement("SELECT COUNT(*) FROM $TABLE_TICKETS").simpleQueryForLong()
            val folio = "TCK-${5000 + count + 1}"

            val cvTicket = ContentValues().apply {
                put("folio", folio)
                put("order_id", orderId)
                put("table_id", tableId)
                put("table_number", tableNumber)
                put("waiter_name", waiterName)
                put("timestamp", now)
                put("subtotal", subtotal)
                put("tax", tax)
                put("tip", tip)
                put("total", total)
                put("payment_method", paymentMethod)
                put("payment_status", "PAGADO")
                put("received_amount", receivedAmount)
                put("change_amount", changeAmount)
            }
            val ticketId = db.insert(TABLE_TICKETS, null, cvTicket)

            // Mark order as PAGADA
            val cvOrder = ContentValues().apply { put("status", "PAGADA") }
            db.update(TABLE_ORDERS, cvOrder, "id = ?", arrayOf(orderId.toString()))

            // Free table
            clearTable(tableId)

            db.setTransactionSuccessful()

            return Ticket(
                id = ticketId,
                folio = folio,
                orderId = orderId,
                tableId = tableId,
                tableNumber = tableNumber,
                waiterName = waiterName,
                timestamp = now,
                subtotal = subtotal,
                tax = tax,
                tip = tip,
                total = total,
                paymentMethod = paymentMethod,
                paymentStatus = "PAGADO",
                receivedAmount = receivedAmount,
                changeAmount = changeAmount
            )
        } finally {
            db.endTransaction()
        }
    }

    fun getAllTickets(): List<Ticket> {
        val list = mutableListOf<Ticket>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TICKETS, null, null, null, null, null, "id DESC")
        while (cursor.moveToNext()) {
            list.add(
                Ticket(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    folio = cursor.getString(cursor.getColumnIndexOrThrow("folio")),
                    orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id")),
                    tableId = cursor.getLong(cursor.getColumnIndexOrThrow("table_id")),
                    tableNumber = cursor.getInt(cursor.getColumnIndexOrThrow("table_number")),
                    waiterName = cursor.getString(cursor.getColumnIndexOrThrow("waiter_name")),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                    subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal")),
                    tax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax")),
                    tip = cursor.getDouble(cursor.getColumnIndexOrThrow("tip")),
                    total = cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
                    paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow("payment_method")),
                    paymentStatus = cursor.getString(cursor.getColumnIndexOrThrow("payment_status")),
                    receivedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("received_amount")),
                    changeAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("change_amount"))
                )
            )
        }
        cursor.close()
        return list
    }

    // ==========================================
    // REPORTS MODULE OPERATIONS
    // ==========================================
    fun getReportSummary(): ReportSummary {
        val db = readableDatabase

        // 1. Total Sales and Paid Tickets
        var totalSales = 0.0
        var paidTicketsCount = 0
        val cursorTickets = db.rawQuery("SELECT SUM(total), COUNT(*) FROM $TABLE_TICKETS WHERE payment_status = 'PAGADO'", null)
        if (cursorTickets.moveToFirst()) {
            totalSales = cursorTickets.getDouble(0)
            paidTicketsCount = cursorTickets.getInt(1)
        }
        cursorTickets.close()

        val averageTicket = if (paidTicketsCount > 0) totalSales / paidTicketsCount else 0.0

        // 2. Total Cancellations
        var totalCancellationsCount = 0
        val cursorCancels = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CANCELLATION_LOGS", null)
        if (cursorCancels.moveToFirst()) {
            totalCancellationsCount = cursorCancels.getInt(0)
        }
        cursorCancels.close()

        // 3. Top Selling Products (From PAID orders only)
        val topProducts = mutableListOf<ProductSales>()
        val cursorTop = db.rawQuery("""
            SELECT oi.product_name, SUM(oi.quantity) as total_qty, SUM(oi.price * oi.quantity) as total_amount
            FROM $TABLE_ORDER_ITEMS oi
            INNER JOIN $TABLE_ORDERS o ON oi.order_id = o.id
            WHERE o.status = 'PAGADA' AND oi.status != 'CANCELADO'
            GROUP BY oi.product_name
            ORDER BY total_qty DESC
            LIMIT 5
        """.trimIndent(), null)
        while (cursorTop.moveToNext()) {
            topProducts.add(
                ProductSales(
                    productName = cursorTop.getString(0),
                    totalQuantity = cursorTop.getInt(1),
                    totalAmount = cursorTop.getDouble(2)
                )
            )
        }
        cursorTop.close()

        // 4. Sales by Waiter (From PAID tickets)
        val salesByWaiter = mutableListOf<WaiterSales>()
        val cursorWaiter = db.rawQuery("""
            SELECT waiter_name, COUNT(*), SUM(total)
            FROM $TABLE_TICKETS
            WHERE payment_status = 'PAGADO'
            GROUP BY waiter_name
            ORDER BY SUM(total) DESC
        """.trimIndent(), null)
        while (cursorWaiter.moveToNext()) {
            salesByWaiter.add(
                WaiterSales(
                    waiterName = cursorWaiter.getString(0),
                    ticketCount = cursorWaiter.getInt(1),
                    totalAmount = cursorWaiter.getDouble(2)
                )
            )
        }
        cursorWaiter.close()

        // 5. Sales by Payment Method
        val salesByPaymentMethod = mutableListOf<PaymentMethodSales>()
        val cursorPay = db.rawQuery("""
            SELECT payment_method, COUNT(*), SUM(total)
            FROM $TABLE_TICKETS
            WHERE payment_status = 'PAGADO'
            GROUP BY payment_method
            ORDER BY SUM(total) DESC
        """.trimIndent(), null)
        while (cursorPay.moveToNext()) {
            salesByPaymentMethod.add(
                PaymentMethodSales(
                    paymentMethod = cursorPay.getString(0),
                    ticketCount = cursorPay.getInt(1),
                    totalAmount = cursorPay.getDouble(2)
                )
            )
        }
        cursorPay.close()

        // 6. Cancellation Motives
        val cancellationMotives = mutableListOf<CancellationMotiveSummary>()
        val cursorMotives = db.rawQuery("""
            SELECT reason, COUNT(*), SUM(price * quantity)
            FROM $TABLE_CANCELLATION_LOGS
            GROUP BY reason
            ORDER BY COUNT(*) DESC
        """.trimIndent(), null)
        while (cursorMotives.moveToNext()) {
            cancellationMotives.add(
                CancellationMotiveSummary(
                    reason = cursorMotives.getString(0),
                    count = cursorMotives.getInt(1),
                    totalAmount = cursorMotives.getDouble(2)
                )
            )
        }
        cursorMotives.close()

        return ReportSummary(
            totalSales = totalSales,
            paidTicketsCount = paidTicketsCount,
            averageTicket = averageTicket,
            totalCancellationsCount = totalCancellationsCount,
            topProducts = topProducts,
            salesByWaiter = salesByWaiter,
            salesByPaymentMethod = salesByPaymentMethod,
            cancellationMotives = cancellationMotives
        )
    }
}
