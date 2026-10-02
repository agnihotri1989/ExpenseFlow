package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Food & Dining", "Transport", "Shopping", "Bills & Utilities", "Entertainment", "Health", "Salary", "Other"
    val type: String,     // "EXPENSE" or "INCOME"
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash", // "Cash", "Credit Card", "Debit Card", "UPI / Bank Transfer"
    val notes: String = ""
)
