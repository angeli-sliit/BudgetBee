package com.example.budgetbee.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee.R
import com.example.budgetbee.databinding.ItemBudgetBinding
import com.example.budgetbee.models.BudgetItem

class BudgetAdapter(private val items: List<BudgetItem>) : 
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: BudgetItem) {
            binding.apply {
                tvCategory.text = item.category
                tvBudgetAmount.text = "${item.currency}${"%.2f".format(item.budgetAmount)}"
                tvSpentAmount.text = "${item.currency}${"%.2f".format(item.spentAmount)}"
                progressBar.progress = item.progress

                // Color coding based on progress
                progressBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        root.context,
                        when {
                            item.progress >= 100 -> R.color.expense_color
                            item.progress >= 80 -> R.color.accent
                            else -> R.color.primary
                        }
                    )
                )
            }
        }
    }
} 