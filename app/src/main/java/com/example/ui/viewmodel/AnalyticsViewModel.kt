package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

data class AnalyticsState(
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val savingsRatePercentage: Float = 0f,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val topCategory: String = "None",
    val topCategoryAmount: Double = 0.0,
    val transactionCount: Int = 0
)

/**
 * Analytics ViewModel demonstrating partial Hilt injection:
 * @HiltViewModel and @Inject constructor applied here.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val analyticsState: StateFlow<AnalyticsState> = repository.allExpensesFlow
        .map { expenses ->
            calculateAnalytics(expenses)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsState()
        )

    private fun calculateAnalytics(expenses: List<ExpenseEntity>): AnalyticsState {
        val totalExpense = expenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalIncome = expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).toFloat().coerceIn(-100f, 100f) else 0f

        val expenseList = expenses.filter { it.type == "EXPENSE" }
        val categoryGroups = expenseList.groupBy { it.category }

        val categorySpends = categoryGroups.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalExpense > 0) ((sum / totalExpense) * 100).toFloat() else 0f
            CategorySpend(category = cat, amount = sum, percentage = pct, count = list.size)
        }.sortedByDescending { it.amount }

        val top = categorySpends.firstOrNull()

        return AnalyticsState(
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            netSavings = netSavings,
            savingsRatePercentage = savingsRate,
            categoryBreakdown = categorySpends,
            topCategory = top?.category ?: "None",
            topCategoryAmount = top?.amount ?: 0.0,
            transactionCount = expenses.size
        )
    }

    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(repository) as T
        }
    }
}
