package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.CartEntity
import com.example.data.database.OrderEntity
import com.example.data.model.Category
import com.example.data.model.MenuCatalog
import com.example.data.model.MenuItem
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    // Filtering State
    private val _selectedCategory = MutableStateFlow<Category?>(null) // null means All
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Expose filtered menu list
    val filteredMenuItems: StateFlow<List<MenuItem>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        var list = MenuCatalog.items
        if (category != null) {
            list = list.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MenuCatalog.items
    )

    // Cart State
    val cartItems: StateFlow<List<CartEntity>> = repository.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartTotal: StateFlow<Int> = cartItems
        .map { list -> list.sumOf { it.price * it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val cartCount: StateFlow<Int> = cartItems
        .map { list -> list.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // History & Tracking States
    val historyOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _trackedOrder = MutableStateFlow<OrderEntity?>(null)
    val trackedOrder: StateFlow<OrderEntity?> = _trackedOrder.asStateFlow()

    // Tab Selection State: 0 = Menu (點餐), 1 = History (訂單紀錄)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    // Cart Actions
    fun addToCart(
        menuItem: MenuItem,
        qty: Int = 1,
        ice: String = "",
        sugar: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            // Check if exact same item (id & customizations) exists in cart
            val currentList = cartItems.value
            val existingItem = currentList.find {
                it.menuItemId == menuItem.id &&
                it.iceLevel == ice &&
                it.sugarLevel == sugar &&
                it.notes == notes
            }

            if (existingItem != null) {
                // Update quantity
                repository.updateCartItem(
                    existingItem.copy(quantity = existingItem.quantity + qty)
                )
            } else {
                // Insert new
                repository.insertCartItem(
                    CartEntity(
                        menuItemId = menuItem.id,
                        name = menuItem.name,
                        price = menuItem.price,
                        quantity = qty,
                        iceLevel = ice,
                        sugarLevel = sugar,
                        notes = notes
                    )
                )
            }
        }
    }

    fun incrementCartItem(cartItem: CartEntity) {
        viewModelScope.launch {
            repository.updateCartItem(cartItem.copy(quantity = cartItem.quantity + 1))
        }
    }

    fun decrementCartItem(cartItem: CartEntity) {
        viewModelScope.launch {
            if (cartItem.quantity > 1) {
                repository.updateCartItem(cartItem.copy(quantity = cartItem.quantity - 1))
            } else {
                repository.deleteCartItem(cartItem)
            }
        }
    }

    fun removeCartItem(cartItem: CartEntity) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItem)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Checkout Actions
    fun placeOrder(orderType: String, tableNo: String, notes: String) {
        viewModelScope.launch {
            val list = cartItems.value
            if (list.isEmpty()) return@launch

            // Generate summary
            val summary = list.joinToString(", ") { item ->
                var customDetail = ""
                val details = mutableListOf<String>()
                if (item.iceLevel.isNotBlank()) details.add(item.iceLevel)
                if (item.sugarLevel.isNotBlank()) details.add(item.sugarLevel)
                if (item.notes.isNotBlank()) details.add(item.notes)
                if (details.isNotEmpty()) {
                    customDetail = " (${details.joinToString("/")})"
                }
                "${item.name}$customDetail x${item.quantity}"
            }

            val finalTableNumber = if (orderType == "內用") {
                if (tableNo.isBlank()) "隨機桌號" else "${tableNo} 號桌"
            } else {
                "外帶自取"
            }

            val total = cartTotal.value

            val order = OrderEntity(
                summaryText = summary,
                totalPrice = total,
                orderType = orderType,
                tableNumber = finalTableNumber,
                status = "製作中...",
                notes = notes
            )

            // Insert into local Room database
            val orderId = repository.insertOrder(order)
            val insertedOrder = order.copy(id = orderId.toInt())

            // Update tracked order & clear current cart
            _trackedOrder.value = insertedOrder
            repository.clearCart()

            // Simulate kitchen preparing updates for realism & delight!
            simulateOrderStatusUpdates(insertedOrder.id)
        }
    }

    private fun simulateOrderStatusUpdates(orderId: Int) {
        viewModelScope.launch {
            // Step 1: 製作中 (12 seconds)
            delay(12000)
            repository.updateOrderStatus(orderId, "主廚烹調中")
            updateTrackedIfMatching(orderId, "主廚烹調中")

            // Step 2: 包裝配送 / 備餐中 (12 seconds)
            delay(12000)
            repository.updateOrderStatus(orderId, "已完成！請至櫃檯取餐")
            updateTrackedIfMatching(orderId, "已完成！請至櫃檯取餐")
        }
    }

    private fun updateTrackedIfMatching(orderId: Int, status: String) {
        val current = _trackedOrder.value
        if (current != null && current.id == orderId) {
            _trackedOrder.value = current.copy(status = status)
        }
    }

    fun dismissTracking() {
        _trackedOrder.value = null
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllOrders()
        }
    }
}

class FoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
