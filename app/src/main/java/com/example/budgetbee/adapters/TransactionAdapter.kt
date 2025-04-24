package com.example.budgetbee.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee.R
import com.example.budgetbee.databinding.ItemTransactionBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionType
import com.example.budgetbee.utils.SharedPrefHelper
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    var transactions: List<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit,
    private val sharedPrefHelper: SharedPrefHelper,
    private val context: Context
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                transactionTitle.text = transaction.title
                transactionAmount.text = sharedPrefHelper.getFormattedAmount(transaction.amount)
                transactionCategory.text = transaction.category
                transactionDate.text = dateFormat.format(transaction.date)
                transactionType.text = transaction.type?.name ?: "Unknown"

                if (transaction.type == TransactionType.EXPENSE) {
                    transactionAmount.setTextColor(context.getColor(R.color.expense_color))
                } else {
                    transactionAmount.setTextColor(context.getColor(R.color.income_color))
                }

                root.setOnClickListener { onEdit(transaction) }
                root.setOnLongClickListener {
                    onDelete(transaction)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}