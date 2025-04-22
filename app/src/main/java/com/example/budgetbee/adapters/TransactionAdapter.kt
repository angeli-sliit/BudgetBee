package com.example.budgetbee.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee.R
import com.example.budgetbee.databinding.ItemTransactionBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.utils.SharedPrefHelper

class TransactionAdapter(
    var transactions: List<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit,
    private val sharedPrefHelper: SharedPrefHelper,
    private val context: Context
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                txtTitle.text = transaction.title
                txtAmount.text = sharedPrefHelper.getFormattedAmount(transaction.amount)
                txtCategory.text = transaction.category
                txtDate.text = transaction.date

                if (transaction.type == "Expense") {
                    txtAmount.setTextColor(context.getColor(R.color.expense_color))
                } else {
                    txtAmount.setTextColor(context.getColor(R.color.income_color))
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