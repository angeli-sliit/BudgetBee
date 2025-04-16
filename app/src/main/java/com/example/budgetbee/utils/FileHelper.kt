package com.example.budgetbee.utils

import android.content.Context
import com.example.budgetbee.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

object FileHelper {
    private const val BACKUP_FILE = "budgetbee_backup.json"

    fun exportToFile(context: Context, transactions: List<Transaction>): Boolean {
        return try {
            val json = Gson().toJson(transactions)
            context.openFileOutput(BACKUP_FILE, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun importFromFile(context: Context): List<Transaction>? {
        return try {
            context.openFileInput(BACKUP_FILE).bufferedReader().use {
                val json = it.readText()
                val type = object : TypeToken<List<Transaction>>() {}.type
                Gson().fromJson(json, type)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun backupExists(context: Context): Boolean {
        return context.getFileStreamPath(BACKUP_FILE).exists()
    }
}