package com.example.budgetbee.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee.databinding.ItemTransactionBinding
import com.example.budgetbee.models.Transaction

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                txtTitle.text = transaction.title
                txtAmount.text = "${if (transaction.type == "Expense") "-" else "+"}$${"%.2f".format(transaction.amount)}"
                txtCategory.text = transaction.category
                txtDate.text = transaction.date

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