package com.example.data.repository

import com.example.data.database.CartEntity
import com.example.data.database.FoodDao
import com.example.data.database.OrderEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    val cartItems: Flow<List<CartEntity>> = foodDao.getCartItems()
    val allOrders: Flow<List<OrderEntity>> = foodDao.getAllOrders()

    suspend fun insertCartItem(item: CartEntity) {
        foodDao.insertCartItem(item)
    }

    suspend fun updateCartItem(item: CartEntity) {
        foodDao.updateCartItem(item)
    }

    suspend fun deleteCartItem(item: CartEntity) {
        foodDao.deleteCartItem(item)
    }

    suspend fun clearCart() {
        foodDao.clearCart()
    }

    suspend fun insertOrder(order: OrderEntity): Long {
        return foodDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(id: Int, status: String) {
        foodDao.updateOrderStatus(id, status)
    }

    suspend fun clearAllOrders() {
        foodDao.clearAllOrders()
    }
}
