package com.example.budgetbee.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.budgetbee.R
import com.example.budgetbee.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.io.File

object FileHelper {
    private const val BACKUP_FILE = "budgetbee_backup.json"

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToJson(context: Context, transactions: List<Transaction>, userId: Long): Boolean {
        if (transactions.isEmpty()) {
            Log.w("JSON Export", "No transactions to export")
            return false
        }
        return try {
            val json = Gson().toJson(transactions)
            val fileName = "budget_export_${userId}_${System.currentTimeMillis()}.json"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: run {
                Log.e("JSON Export", "Failed to create file")
                return false
            }
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
            } ?: run {
                Log.e("JSON Export", "Failed to open output stream")
                return false
            }
            Log.i("JSON Export", "Successfully exported ${transactions.size} transactions")
            true
        } catch (e: Exception) {
            Log.e("JSON Export", "Error: ${e.message}", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun importFromJson(context: Context, uri: Uri): List<Transaction>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().readText()
                if (json.isBlank()) {
                    Log.e("JSON Import", "Empty file")
                    return null
                }

                val type = object : TypeToken<List<Transaction>>() {}.type
                val transactions = Gson().fromJson<List<Transaction>>(json, type)

                if (transactions.isNullOrEmpty()) {
                    Log.e("JSON Import", "No valid transactions found")
                    return null
                }

                Log.i("JSON Import", "Successfully imported ${transactions.size} transactions")
                transactions
            } ?: run {
                Log.e("JSON Import", "Failed to open input stream")
                null
            }
        } catch (e: Exception) {
            Log.e("JSON Import", "Error: ${e.message}", e)
            null
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToDownloads(
        context: Context,
        content: String,
        mimeType: String,
        fileName: String
    ): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false
            resolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
            true
        } catch (e: Exception) {
            Log.e("FileHelper", "Save failed", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportBudgetPdf(context: Context, sharedPrefHelper: SharedPrefHelper, userId: String, transactions: List<Transaction>): Boolean {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val pdfDocument = PdfDocument()

        return try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            var yPos: Float

            // Draw logo
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.oip_removebg_preview__1_)
            canvas.drawBitmap(logoBitmap, null, android.graphics.Rect(40, 30, 140, 100), null)

            // Title
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText("Budget Report - $currentMonth", 160f, 70f, paint)

            // Draw horizontal divider
            paint.strokeWidth = 1.5f
            canvas.drawLine(40f, 110f, 555f, 110f, paint)
            yPos = 130f

            // Section Title: Summary
            paint.textSize = 16f
            paint.color = android.graphics.Color.DKGRAY
            paint.isFakeBoldText = true
            canvas.drawText("Summary", 40f, yPos, paint)
            yPos += 10f

            // Line under Summary
            paint.isFakeBoldText = false
            canvas.drawLine(40f, yPos, 555f, yPos, paint)
            yPos += 20f

            // Budget Data
            val monthlyTransactions = transactions.filter { it.date.startsWith(currentMonth) }
            val income = monthlyTransactions.filter { it.type.equals("income", ignoreCase = true) }.sumOf { it.amount }
            val expenses = monthlyTransactions.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amount }
            val budget = sharedPrefHelper.monthlyBudget
            val currency = "USD" // TODO: Replace with user's currency if available

            paint.textSize = 14f
            canvas.drawText("Monthly Budget  : ${sharedPrefHelper.getFormattedAmount(budget, currency)}", 40f, yPos, paint.apply { color = android.graphics.Color.BLUE })
            yPos += 20f
            canvas.drawText("Total Income        : ${sharedPrefHelper.getFormattedAmount(income, currency)}", 40f, yPos, paint.apply { color = android.graphics.Color.rgb(0, 150, 0) })
            yPos += 20f
            canvas.drawText("Total Expenses    : ${sharedPrefHelper.getFormattedAmount(expenses, currency)}", 40f, yPos, paint.apply { color = android.graphics.Color.RED })
            yPos += 50f

            // Section Title: Expenses
            paint.textSize = 16f
            paint.color = android.graphics.Color.DKGRAY
            paint.isFakeBoldText = true
            canvas.drawText("Expense Categories", 40f, yPos, paint)
            yPos += 10f
            paint.isFakeBoldText = false
            canvas.drawLine(40f, yPos, 555f, yPos, paint)
            yPos += 20f

            // Category breakdown
            paint.textSize = 14f
            paint.color = android.graphics.Color.BLACK
            monthlyTransactions.filter { it.type.equals("expense", ignoreCase = true) }
                .groupBy { it.category }
                .forEach { (category, trans) ->
                    val sum = trans.sumOf { it.amount }
                    canvas.drawText("$category : ${sharedPrefHelper.getFormattedAmount(sum, currency)}", 40f, yPos, paint)
                    yPos += 20f
                }

            pdfDocument.finishPage(page)

            // Save to Downloads folder
            val fileName = "BudgetReport_${userId}${currentMonth}${System.currentTimeMillis()}.pdf"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return false
            resolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            } ?: return false

            true
        } catch (e: Exception) {
            Log.e("PDF Export", "Error: ${e.message}", e)
            false
        } finally {
            pdfDocument.close()
        }
    }

    private fun createTransaction(category: String, amount: Double, userId: Long): Transaction {
        return Transaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = "Category: $category",
            amount = amount,
            type = "Expense",
            category = category,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
}