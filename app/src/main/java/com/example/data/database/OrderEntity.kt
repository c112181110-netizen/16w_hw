package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val summaryText: String, // e.g., "經典排骨飯 x1, 珍珠鮮奶茶 x1(少冰/半糖)"
    val totalPrice: Int,
    val orderType: String, // "內用" / "外帶"
    val tableNumber: String, // e.g., "3 號桌" or "外帶自取"
    val status: String, // "Preparing" (製作中), "Cooking" (烹調中), "Ready" (已完成，請取餐)
    val notes: String = "" // Special chef note
)
