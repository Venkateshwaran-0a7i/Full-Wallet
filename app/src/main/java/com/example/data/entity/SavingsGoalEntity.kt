package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)
