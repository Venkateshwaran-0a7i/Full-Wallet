package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_expenses")
data class DailyExpenseEntity(
    @PrimaryKey val id: String,
    val date: String, // Format: "yyyy-MM-dd"
    val category: String,
    val amount: Double,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
