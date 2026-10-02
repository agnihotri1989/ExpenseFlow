package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val transactions: List<ExpenseEntity> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedFilter: String = "ALL", // "ALL", "EXPENSE", "INCOME"
    val selectedCategory: String = "All",
    val searchQuery: String = ""
)

/**
 * Loosely MVVM ViewModel managing dashboard and transactions.
 */
class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allExpensesFlow,
        _selectedFilter,
        _selectedCategory,
        _searchQuery
    ) { expenses, filter, category, query ->
        val totalIncome = expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = expenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense

        val filtered = expenses.filter { item ->
            val matchesFilter = when (filter) {
                "EXPENSE" -> item.type == "EXPENSE"
                "INCOME" -> item.type == "INCOME"
                else -> true
            }
            val matchesCategory = if (category == "All") true else item.category.equals(category, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                item.title.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true) ||
                item.notes.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesCategory && matchesQuery
        }

        DashboardUiState(
            transactions = filtered,
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            selectedFilter = filter,
            selectedCategory = category,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        type: String,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title,
                amount = amount,
                category = category,
                type = type,
                date = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                notes = notes
            )
            repository.insertExpense(expense)
        }
    }

    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseViewModel(repository) as T
        }
    }
}
