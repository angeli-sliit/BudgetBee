package com.example.budgetbee.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee.R
import com.example.budgetbee.databinding.ItemTransactionBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.utils.SharedPrefHelper

class TransactionAdapter(
    var transactions: List<Transaction>,
    currencyCode: String,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var currencyCode: String = currencyCode
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    companion object {
        private const val VIEW_TYPE_TRANSACTION = 0
        private const val VIEW_TYPE_EMPTY = 1
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                txtTitle.text = transaction.title
                txtAmount.text = SharedPrefHelper(root.context).getFormattedAmount(transaction.amount, currencyCode)
                txtCategory.text = transaction.category
                txtDate.text = transaction.date

                if (transaction.type == "Expense") {
                    txtAmount.setTextColor(root.context.getColor(R.color.expense_color))
                } else {
                    txtAmount.setTextColor(root.context.getColor(R.color.income_color))
                }

                root.setOnClickListener { onEdit(transaction) }
                root.setOnLongClickListener {
                    onDelete(transaction)
                    true
                }
            }
        }
    }

    inner class EmptyViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                txtTitle.text = "No transactions yet"
                txtAmount.visibility = View.GONE
                txtCategory.visibility = View.GONE
                txtDate.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (transactions.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return when (viewType) {
            VIEW_TYPE_TRANSACTION -> TransactionViewHolder(binding)
            VIEW_TYPE_EMPTY -> EmptyViewHolder(binding)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.bind(transactions[position])
            is EmptyViewHolder -> { /* Empty state is handled in the ViewHolder */ }
        }
    }

    override fun getItemCount() = if (transactions.isEmpty()) 1 else transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
