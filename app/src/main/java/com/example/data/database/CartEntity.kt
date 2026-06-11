package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val menuItemId: Int,
    val name: String,
    val price: Int,
    val quantity: Int,
    val iceLevel: String = "",
    val sugarLevel: String = "",
    val notes: String = ""
)
