package com.example.budgetbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbee.adapters.BudgetAdapter
import com.example.budgetbee.databinding.FragmentBudgetBinding
import com.example.budgetbee.models.BudgetItem
import com.example.budgetbee.models.TransactionType
import com.example.budgetbee.utils.SharedPrefHelper
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val monthlyBudget = sharedPrefHelper.monthlyBudget
        val transactions = sharedPrefHelper.getTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        val monthlyTransactions = transactions.filter { 
            val transactionMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(it.date)
            transactionMonth == currentMonth 
        }

        val totalExpenses = monthlyTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (category, transactions) ->
                BudgetItem(
                    category = category,
                    budgetAmount = monthlyBudget / 5, // Dividing total budget equally among categories
                    spentAmount = transactions.sumOf { it.amount },
                    currency = sharedPrefHelper.currency
                )
            }
            .sortedByDescending { it.spentAmount }

        if (totalExpenses.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerBudgets.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerBudgets.visibility = View.VISIBLE
            
            budgetAdapter = BudgetAdapter(totalExpenses)
            binding.recyclerBudgets.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = budgetAdapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 