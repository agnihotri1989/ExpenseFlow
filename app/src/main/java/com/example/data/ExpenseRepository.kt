package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Functional callback interface for seamless Java interop.
 */
fun interface OnResultCallback<T> {
    fun onResult(result: T)
}

/**
 * Repository demonstrating loosely MVVM architecture.
 * Supports both modern coroutines Flow for Compose ViewModels
 * and background executors/callbacks for legacy Java activities.
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    private val executor = Executors.newSingleThreadExecutor()

    val allExpensesFlow: Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()

    suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteById(id)
    }

    suspend fun getAllExpenses(): List<ExpenseEntity> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpensesSync()
    }

    // --- Legacy Java-compatible helper methods with SAM interfaces ---
    fun insertExpenseAsync(expense: ExpenseEntity, callback: OnResultCallback<Long>?) {
        executor.execute {
            val id = expenseDao.insertExpenseSync(expense)
            callback?.onResult(id)
        }
    }

    fun getAllExpensesAsync(callback: OnResultCallback<List<ExpenseEntity>>) {
        executor.execute {
            val list = expenseDao.getAllExpensesSync()
            callback.onResult(list)
        }
    }

    fun deleteByIdAsync(id: Long, callback: OnResultCallback<Boolean>?) {
        executor.execute {
            val rows = expenseDao.deleteByIdSync(id)
            callback?.onResult(rows > 0)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ExpenseRepository? = null

        fun getInstance(context: Context): ExpenseRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val repo = ExpenseRepository(db.expenseDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
