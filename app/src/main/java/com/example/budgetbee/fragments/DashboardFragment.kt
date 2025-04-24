package com.example.budgetbee.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.budgetbee.R
import com.example.budgetbee.databinding.FragmentDashboardBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.models.TransactionType
import com.example.budgetbee.utils.SharedPrefHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val transactions = sharedPrefHelper.getTransactions()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        val monthlyTransactions = transactions.filter { 
            val transactionMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(it.date)
            transactionMonth == currentMonth 
        }
        val totalIncome = monthlyTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        binding.apply {
            tvIncome.text = "${sharedPrefHelper.currency}${"%.2f".format(totalIncome)}"
            tvExpenses.text = "${sharedPrefHelper.currency}${"%.2f".format(totalExpenses)}"
            tvBalance.text = "${sharedPrefHelper.currency}${"%.2f".format(balance)}"

            // Budget progress
            val budget = sharedPrefHelper.monthlyBudget
            if (budget > 0) {
                val progress = (totalExpenses / budget * 100).toInt().coerceIn(0, 100)
                progressBar.progress = progress
                tvBudgetProgress.text = "$progress% of budget used"

                // Color coding
                progressBar.progressTintList = ColorStateList.valueOf(
                    when {
                        progress >= 100 -> ContextCompat.getColor(requireContext(), R.color.expense_color)
                        progress >= 80 -> ContextCompat.getColor(requireContext(), R.color.accent)
                        else -> ContextCompat.getColor(requireContext(), R.color.primary)
                    }
                )
            } else {
                progressBar.visibility = View.GONE
                tvBudgetProgress.text = "No budget set"
            }

            // Setup pie chart
            setupPieChart(binding.pieChart, monthlyTransactions)
        }
    }

    private fun setupPieChart(pieChart: PieChart, transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        if (expenseTransactions.isEmpty()) {
            pieChart.visibility = View.GONE
            return
        }

        pieChart.visibility = View.VISIBLE

        val categoryMap = expenseTransactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        val entries = categoryMap.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.primary),
                ContextCompat.getColor(requireContext(), R.color.primary_dark),
                ContextCompat.getColor(requireContext(), R.color.secondary),
                ContextCompat.getColor(requireContext(), R.color.button_color),
                ContextCompat.getColor(requireContext(), R.color.expense_color),
                ContextCompat.getColor(requireContext(), R.color.accent),
                ContextCompat.getColor(requireContext(), R.color.income_color)
            )
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            valueTextSize = 12f
        }

        pieChart.apply {
            data = PieData(dataSet).apply {
                setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setValueTextSize(12f)
            }
            description.isEnabled = false
            legend.apply {
                isEnabled = true
                textColor = ContextCompat.getColor(requireContext(), R.color.black)
                textSize = 12f
            }
            setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
            setEntryLabelTextSize(12f)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        }

        binding.btnViewAllCategories.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_categoryAnalysisFragment)
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