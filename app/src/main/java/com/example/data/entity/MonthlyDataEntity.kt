package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_data")
data class MonthlyDataEntity(
    @PrimaryKey val id: Int = 1,
    val income: Double? = null,
    val bonus: Double? = null,
    val goal: String = "",
    val groceries: Double? = null,
    val medicine: Double? = null,
    val education: Double? = null,
    val rent: Double? = null,
    val food: Double? = null,
    val transport: Double? = null,
    val electricity: Double? = null,
    val internet: Double? = null,
    val mobile: Double? = null,
    val emi: Double? = null,
    val subscriptions: Double? = null,
    val shopping: Double? = null,
    val entertainment: Double? = null,
    val investments: Double? = null,
    val savings: Double? = null,
    val others: Double? = null
)
