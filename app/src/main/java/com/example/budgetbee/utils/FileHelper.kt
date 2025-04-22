package com.example.budgetbee.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
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
            Log.e("FileHelper", "Export failed: ${e.message}", e)
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
            Log.e("FileHelper", "Import failed: ${e.message}", e)
            null
        }
    }

    fun backupExists(context: Context): Boolean {
        return context.getFileStreamPath(BACKUP_FILE).exists()
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportBudgetPdf(context: Context, sharedPrefHelper: SharedPrefHelper): Boolean {
        val transactions = sharedPrefHelper.getTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val pdfDocument = PdfDocument()

        return try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            var yPos = 100f

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
            val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
            val expenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val budget = sharedPrefHelper.monthlyBudget

            paint.textSize = 14f
            canvas.drawText("Monthly Budget  : ${sharedPrefHelper.getFormattedAmount(budget)}", 40f, yPos, paint.apply { color = android.graphics.Color.BLUE })
            yPos += 20f
            canvas.drawText("Total Income        : ${sharedPrefHelper.getFormattedAmount(income)}", 40f, yPos, paint.apply { color = android.graphics.Color.rgb(0, 150, 0) })
            yPos += 20f
            canvas.drawText("Total Expenses    : ${sharedPrefHelper.getFormattedAmount(expenses)}", 40f, yPos, paint.apply { color = android.graphics.Color.RED })
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
            transactions.filter { it.type == "Expense" }
                .groupBy { it.category }
                .forEach { (category, trans) ->
                    val sum = trans.sumOf { it.amount }
                    canvas.drawText("$category : ${sharedPrefHelper.getFormattedAmount(sum)}", 40f, yPos, paint)
                    yPos += 20f
                }

            pdfDocument.finishPage(page)

            // Save to Downloads folder
            val resolver = context.contentResolver
            val fileName = "BudgetReport_$currentMonth.pdf"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return false
            resolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }

            NotificationHelper.showExportSuccess(context)
            true
        } catch (e: Exception) {
            Log.e("PDF Export", "Error: ${e.message}", e)
            NotificationHelper.showExportFailure(context)
            false
        } finally {
            pdfDocument.close()
        }
    }
}