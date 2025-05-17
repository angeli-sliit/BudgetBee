package com.example.budgetbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.budgetbee.R
import com.example.budgetbee.databinding.FragmentCategoryAnalysisBinding
import com.example.budgetbee.viewModel.CategoryViewModel
import com.example.budgetbee.viewModel.CategoryViewModelFactory
import com.example.budgetbee.viewModel.TransactionViewModel
import com.google.android.material.snackbar.Snackbar
import android.graphics.drawable.GradientDrawable
import androidx.cardview.widget.CardView
import com.example.budgetbee.utils.SharedPrefHelper
import com.example.budgetbee.viewModel.UserViewModel
import com.example.budgetbee.adapters.CategoryAdapter

class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(requireActivity().application)
    }
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var lastTransactions: List<com.example.budgetbee.models.Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressCategoryLoading.visibility = View.VISIBLE
        binding.layoutCategoryAnalysis.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        setupViews()
        observeCategories()
        // Observe user changes robustly
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                observeTransactions(user.id)
                updateCategoryAnalysis(lastTransactions)
            }
        }
        // Setup CategoryAdapter with currency from UserViewModel
        val currency = userViewModel.currentUser.value?.currency ?: "USD"
        CategoryAdapter(currency) { /* onItemClick logic */ }
        // Set adapter to your RecyclerView if needed
    }

    private fun setupViews() {
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) {
            // Update UI with categories
        }
    }

    private fun observeTransactions(userId: Long) {
        transactionViewModel.setCurrentUserId(userId)
        transactionViewModel.getTransactions().observe(viewLifecycleOwner) { transactions ->
            binding.progressCategoryLoading.visibility = View.GONE
            binding.layoutCategoryAnalysis.visibility = View.VISIBLE
            lastTransactions = transactions
            updateCategoryAnalysis(transactions)
        }
    }

    private fun updateCategoryAnalysis(transactions: List<com.example.budgetbee.models.Transaction>) {
        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
        val monthlyExpenses = transactions.filter { it.type.equals("expense", ignoreCase = true) && it.date.startsWith(currentMonth) }
        val categoryMap = monthlyExpenses.groupBy { it.category }
            .mapValues { (_, trans) -> trans.sumOf { it.amount } }
        binding.root.findViewById<android.widget.LinearLayout>(R.id.layoutCategoryAnalysis)?.let { layout ->
            layout.removeAllViews()
            if (categoryMap.isEmpty()) {
                val emptyView = android.widget.TextView(requireContext())
                emptyView.text = "No expenses for this month."
                emptyView.textSize = 18f
                emptyView.setPadding(0, 32, 0, 0)
                layout.addView(emptyView)
            } else {
                val colors = listOf(
                    0xFFE57373.toInt(), // Red
                    0xFF64B5F6.toInt(), // Blue
                    0xFF81C784.toInt(), // Green
                    0xFFFFB74D.toInt(), // Orange
                    0xFFBA68C8.toInt(), // Purple
                    0xFFFF8A65.toInt()  // Deep Orange
                )
                var colorIndex = 0
                val currency = userViewModel.currentUser.value?.currency ?: "USD"
                for ((category, amount) in categoryMap) {
                    val card = CardView(requireContext())
                    val params = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 24)
                    card.layoutParams = params
                    card.radius = 24f
                    card.cardElevation = 8f
                    card.setContentPadding(32, 24, 32, 24)

                    val row = android.widget.LinearLayout(requireContext())
                    row.orientation = android.widget.LinearLayout.HORIZONTAL
                    row.layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    row.gravity = android.view.Gravity.CENTER_VERTICAL

                    // Icon
                    val icon = android.view.View(requireContext())
                    val iconParams = android.widget.LinearLayout.LayoutParams(48, 48)
                    iconParams.setMargins(0, 0, 32, 0)
                    icon.layoutParams = iconParams
                    val drawable = GradientDrawable()
                    drawable.shape = GradientDrawable.OVAL
                    drawable.setColor(colors[colorIndex % colors.size])
                    icon.background = drawable

                    // Category name
                    val catView = android.widget.TextView(requireContext())
                    catView.text = category
                    catView.textSize = 18f
                    catView.setTypeface(null, android.graphics.Typeface.BOLD)
                    catView.layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                    // Amount
                    val amtView = android.widget.TextView(requireContext())
                    amtView.text = SharedPrefHelper(requireContext()).getFormattedAmount(amount, currency)
                    amtView.textSize = 18f
                    amtView.setTypeface(null, android.graphics.Typeface.BOLD)
                    amtView.layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    amtView.textAlignment = android.view.View.TEXT_ALIGNMENT_VIEW_END

                    row.addView(icon)
                    row.addView(catView)
                    row.addView(amtView)
                    card.addView(row)
                    layout.addView(card)
                    colorIndex++
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        viewModel.createCategory(
            name = "New Category",
            iconResId = R.drawable.ic_category,
            color = requireContext().getColor(R.color.primary),
            type = "Expense"
        )
        showMessage("Category added successfully")
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error_color))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
