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
import com.example.budgetbee.utils.SharedPrefHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*
import com.example.budgetbee.viewModel.UserViewModel
import androidx.fragment.app.viewModels
import android.widget.TextView
import com.example.budgetbee.viewModel.TransactionViewModel
import com.example.budgetbee.viewModel.BudgetViewModel
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private val userViewModel: UserViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private var lastTransactions: List<Transaction> = emptyList()

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
        setupListeners()
        // Observe user changes robustly
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                budgetViewModel.setCurrentUserId(user.id.toString())
                budgetViewModel.loadData()
                transactionViewModel.setCurrentUserId(user.id)
                observeTransactions()
                updateDashboardViews(lastTransactions)
            } else {
                binding.progressDashboardLoading.visibility = View.GONE
                binding.dashboardContent.visibility = View.GONE
            }
        }
        // Observe budget changes and update progress bar
        budgetViewModel.totalBudget.observe(viewLifecycleOwner) { totalBudget ->
            updateDashboardViews(lastTransactions)
        }
        // Observe budget progress and update progress bar and text
        budgetViewModel.budgetProgress.observe(viewLifecycleOwner) { (progress, message) ->
            binding.progressBar.progress = progress
            binding.tvBudgetProgress.text = message
        }
    }

    private fun setupViews() {
        // No need to call setupViews() here, as LiveData observer will update UI
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        }

        binding.btnViewAllCategories.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_categoryAnalysisFragment)
        }
    }

    private fun observeTransactions() {
        binding.progressDashboardLoading.visibility = View.VISIBLE
        binding.dashboardContent.visibility = View.GONE
        val currentUser = userViewModel.currentUser.value
        if (currentUser == null) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content), "Please login first", Snackbar.LENGTH_LONG).show()
            return
        }
        transactionViewModel.setCurrentUserId(currentUser.id)
        budgetViewModel.setCurrentUserId(currentUser.id.toString())
        transactionViewModel.getTransactions().observe(viewLifecycleOwner) { transactions ->
            binding.progressDashboardLoading.visibility = View.GONE
            binding.dashboardContent.visibility = View.VISIBLE
            lastTransactions = transactions
            updateDashboardViews(transactions)
        }
    }

    private fun updateDashboardViews(transactions: List<Transaction>) {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyTransactions = transactions.filter { it.date.startsWith(currentMonth) }
        val totalIncome = monthlyTransactions.filter { it.type.equals("income", ignoreCase = true) }.sumOf { it.amount }
        val totalExpenses = monthlyTransactions.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        binding.apply {
            val currency = userViewModel.currentUser.value?.currency ?: "USD"
            tvIncome.text = sharedPrefHelper.getFormattedAmount(totalIncome, currency)
            tvExpenses.text = sharedPrefHelper.getFormattedAmount(totalExpenses, currency)
            tvBalance.text = sharedPrefHelper.getFormattedAmount(balance, currency)

            // Budget progress
            val totalBudget = budgetViewModel.totalBudget.value ?: 0.0
            if (totalBudget > 0) {
                val progress = (totalExpenses / totalBudget * 100).toInt().coerceIn(0, 100)
                progressBar.progress = progress
                tvBudgetProgress.text = "$progress% of budget used"

                // Update budget progress in ViewModel for notifications
                budgetViewModel.updateBudgetProgress(totalExpenses)

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
            // Setup top categories
            setupTopCategories(monthlyTransactions)
        }
    }

    private fun setupPieChart(pieChart: PieChart, transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.type.equals("expense", ignoreCase = true) }
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
                ContextCompat.getColor(requireContext(), R.color.secondary),
                ContextCompat.getColor(requireContext(), R.color.primary_dark),
                ContextCompat.getColor(requireContext(), R.color.expense_color),
                ContextCompat.getColor(requireContext(), R.color.accent),
                ContextCompat.getColor(requireContext(), R.color.income_color),
                ContextCompat.getColor(requireContext(), R.color.button_color),
            )
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            valueTextSize = 12f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }

    private fun setupTopCategories(transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.type.equals("expense", ignoreCase = true) }
        val categoryMap = expenseTransactions.groupBy { it.category }
            .mapValues { (_, trans) -> trans.sumOf { it.amount } }
        val sortedCategories = categoryMap.entries.sortedByDescending { it.value }.take(3)
        binding.layoutTopCategories.removeAllViews()
        for ((category, amount) in sortedCategories) {
            val view = layoutInflater.inflate(R.layout.item_category_simple, binding.layoutTopCategories, false)
            view.findViewById<TextView>(R.id.tvCategoryName).text = category
            val currency = userViewModel.currentUser.value?.currency ?: "USD"
            view.findViewById<TextView>(R.id.tvCategoryAmount).text = sharedPrefHelper.getFormattedAmount(amount, currency)
            binding.layoutTopCategories.addView(view)
        }
    }

    override fun onResume() {
        super.onResume()
        // No need to call setupViews() here, as LiveData observer will update UI
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createTransaction(category: String, amount: Double): Transaction {
        val currentUser = userViewModel.currentUser.value ?: throw IllegalStateException("User not logged in")
        return Transaction(
            id = UUID.randomUUID().toString(),
            userId = currentUser.id,
            title = "Category: $category",
            amount = amount,
            type = "Expense",
            category = category,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
}
