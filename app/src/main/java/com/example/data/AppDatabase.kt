package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial realistic expenses
                val instance = INSTANCE ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(instance.expenseDao())
                }
            }

            private suspend fun populateInitialData(dao: ExpenseDao) {
                val now = System.currentTimeMillis()
                val oneDay = 86400000L
                val seedData = listOf(
                    ExpenseEntity(
                        title = "Monthly Tech Salary",
                        amount = 4850.00,
                        category = "Salary",
                        type = "INCOME",
                        date = now - oneDay * 2,
                        paymentMethod = "Bank Transfer",
                        notes = "Primary payroll deposit"
                    ),
                    ExpenseEntity(
                        title = "Whole Foods Organic Groceries",
                        amount = 142.75,
                        category = "Food & Dining",
                        type = "EXPENSE",
                        date = now - oneDay * 1,
                        paymentMethod = "Credit Card",
                        notes = "Weekly grocery refill"
                    ),
                    ExpenseEntity(
                        title = "Monthly Apartment Rent",
                        amount = 1250.00,
                        category = "Bills & Utilities",
                        type = "EXPENSE",
                        date = now - oneDay * 3,
                        paymentMethod = "Bank Transfer",
                        notes = "October rent"
                    ),
                    ExpenseEntity(
                        title = "Freelance UI Consulting",
                        amount = 750.00,
                        category = "Income",
                        type = "INCOME",
                        date = now - oneDay * 4,
                        paymentMethod = "UPI / Online",
                        notes = "Design system review contract"
                    ),
                    ExpenseEntity(
                        title = "Subway Pass & Fuel",
                        amount = 58.40,
                        category = "Transport",
                        type = "EXPENSE",
                        date = now - oneDay * 2,
                        paymentMethod = "Debit Card",
                        notes = "Metro card recharge + gas"
                    ),
                    ExpenseEntity(
                        title = "Cinema & Popcorn Night",
                        amount = 36.50,
                        category = "Entertainment",
                        type = "EXPENSE",
                        date = now - oneDay * 5,
                        paymentMethod = "Cash",
                        notes = "Weekend movie with friends"
                    ),
                    ExpenseEntity(
                        title = "Winter Jacket & Boots",
                        amount = 189.90,
                        category = "Shopping",
                        type = "EXPENSE",
                        date = now - oneDay * 6,
                        paymentMethod = "Credit Card",
                        notes = "Seasonal clothing upgrade"
                    ),
                    ExpenseEntity(
                        title = "Dental Cleaning Checkup",
                        amount = 95.00,
                        category = "Health",
                        type = "EXPENSE",
                        date = now - oneDay * 7,
                        paymentMethod = "Debit Card",
                        notes = "Routine 6-month checkup"
                    )
                )
                dao.insertAll(seedData)
            }
        }
    }
}
