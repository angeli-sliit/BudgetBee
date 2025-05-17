package com.example.budgetbee.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, Transaction::class, Budget::class, Category::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create budgets table
                database.execSQL("CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "month TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "createdAt INTEGER NOT NULL DEFAULT 0)")

                // Create categories table
                database.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "iconResId INTEGER NOT NULL, " +
                    "color INTEGER NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "amount REAL NOT NULL DEFAULT 0.0, " +
                    "createdAt INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add any necessary changes for version 3
                // For now, this is just a placeholder migration
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN currency TEXT DEFAULT 'USD' NOT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Create a temp table with unique rows
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets_temp AS
                    SELECT * FROM budgets
                    WHERE id IN (
                        SELECT MIN(id) FROM budgets GROUP BY userId, month, category
                    )
                """)
                // 2. Drop the old budgets table
                database.execSQL("DROP TABLE budgets")
                // 3. Recreate the budgets table with the correct schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        amount REAL NOT NULL,
                        month TEXT NOT NULL,
                        category TEXT NOT NULL,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                // 4. Copy data back from temp table
                database.execSQL("INSERT INTO budgets (id, userId, amount, month, category, createdAt) SELECT id, userId, amount, month, category, createdAt FROM budgets_temp")
                // 5. Drop temp table
                database.execSQL("DROP TABLE budgets_temp")
                // 6. Add unique index
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_userId_month_category ON budgets(userId, month, category)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_bee_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 
